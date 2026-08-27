package com.gamesplatform.gomoku.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamesplatform.common.BusinessException;
import com.gamesplatform.gomoku.domain.GomokuRules;
import com.gamesplatform.gomoku.dto.GomokuGameResponse;
import com.gamesplatform.gomoku.dto.MoveRequest;
import com.gamesplatform.gomoku.entity.GomokuGame;
import com.gamesplatform.gomoku.mapper.GomokuGameMapper;
import com.gamesplatform.game.domain.WaitingRoomResponse;
import com.gamesplatform.points.service.PointsService;
import com.gamesplatform.user.entity.User;
import com.gamesplatform.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** 双人五子棋业务服务。 */
@Service
@RequiredArgsConstructor
public class GomokuService {
    private static final int WIN_POINTS = 10;
    private static final int LOSE_POINTS = 5;
    private static final int MAX_ROOM_CODE_ATTEMPTS = 10;

    private final GomokuGameMapper gameMapper;
    private final UserMapper userMapper;
    private final PointsService pointsService;
    private final ObjectMapper objectMapper;

    /** 创建邀请房间，双方颜色在对手加入时随机分配。 */
    @Transactional
    public GomokuGameResponse createRoom(Long userId) {
        ensureUserExists(userId);
        ensureNoActiveGame(userId);
        for (int attempt = 0; attempt < MAX_ROOM_CODE_ATTEMPTS; attempt++) {
            GomokuGame game = new GomokuGame();
            game.setRoomCode(randomRoomCode());
            game.setBlackPlayerId(userId);
            game.setCurrentPlayerId(userId);
            game.setBoardJson(toJson(new int[GomokuRules.BOARD_SIZE][GomokuRules.BOARD_SIZE]));
            game.setMoveCount(0);
            game.setStatus("WAITING");
            game.setCreatedAt(LocalDateTime.now());
            try {
                gameMapper.insert(game);
                return toResponse(game, userId, null);
            } catch (DuplicateKeyException ignored) {
                // 极低概率房间码冲突，重新生成。
            }
        }
        throw new BusinessException("房间创建失败，请稍后重试");
    }

    /** 通过房间码加入对局，并随机分配双方黑白颜色。 */
    @Transactional
    public GomokuGameResponse joinRoom(Long userId, String rawRoomCode) {
        String roomCode = normalizeRoomCode(rawRoomCode);
        GomokuGame game = gameMapper.selectOne(new LambdaQueryWrapper<GomokuGame>()
                .eq(GomokuGame::getRoomCode, roomCode).last("FOR UPDATE"));
        if (game == null) {
            throw new BusinessException("房间不存在");
        }
        if (userId.equals(game.getBlackPlayerId())) {
            throw new BusinessException("不能加入自己创建的房间");
        }
        if (!"WAITING".equals(game.getStatus()) || game.getWhitePlayerId() != null) {
            throw new BusinessException("房间已满或对局已开始");
        }
        ensureUserExists(userId);
        ensureNoActiveGame(userId);
        Long creatorId = game.getBlackPlayerId();
        boolean creatorPlaysBlack = ThreadLocalRandom.current().nextBoolean();
        if (creatorPlaysBlack) {
            game.setWhitePlayerId(userId);
            game.setCurrentPlayerId(creatorId);
        } else {
            game.setBlackPlayerId(userId);
            game.setWhitePlayerId(creatorId);
            game.setCurrentPlayerId(userId);
        }
        game.setStatus("IN_PROGRESS");
        game.setStartedAt(LocalDateTime.now());
        gameMapper.updateById(game);
        return toResponse(game, userId, null);
    }

    /** 查询对局最新状态，供双方轮询。 */
    public GomokuGameResponse getGame(Long userId, Long gameId) {
        return toResponse(requireParticipant(gameMapper.selectById(gameId), userId), userId, null);
    }

    /** 查询当前用户尚未结束的房间，便于刷新页面后恢复对局。 */
    public GomokuGameResponse getActiveGame(Long userId) {
        GomokuGame game = findActiveGame(userId);
        return game == null ? null : toResponse(game, userId, null);
    }

    /** 查询可加入的等待中房间，不返回当前用户自己创建的房间。 */
    public List<WaitingRoomResponse> getWaitingRooms(Long userId) {
        return gameMapper.selectList(new LambdaQueryWrapper<GomokuGame>()
                        .eq(GomokuGame::getStatus, "WAITING")
                        .ne(GomokuGame::getBlackPlayerId, userId)
                        .orderByAsc(GomokuGame::getCreatedAt)
                        .last("LIMIT 50"))
                .stream()
                .map(game -> {
                    User host = userMapper.selectById(game.getBlackPlayerId());
                    return WaitingRoomResponse.builder()
                            .id(game.getId())
                            .roomCode(game.getRoomCode())
                            .hostName(host == null ? "未知玩家" : host.getNickname())
                            .hostUsername(host == null ? "未知用户" : host.getUsername())
                            .build();
                }).toList();
    }

    /** 落子并在服务端判断胜负。 */
    @Transactional
    public GomokuGameResponse move(Long userId, Long gameId, MoveRequest request) {
        GomokuGame game = lockGame(gameId);
        requireParticipant(game, userId);
        if (!"IN_PROGRESS".equals(game.getStatus())) {
            throw new BusinessException("对局尚未开始或已经结束");
        }
        if (!userId.equals(game.getCurrentPlayerId())) {
            throw new BusinessException("还没有轮到你落子");
        }
        if (request == null || request.getRow() == null || request.getCol() == null
                || request.getRow() < 0 || request.getRow() >= GomokuRules.BOARD_SIZE
                || request.getCol() < 0 || request.getCol() >= GomokuRules.BOARD_SIZE) {
            throw new BusinessException("落子位置无效");
        }

        int[][] board = fromJson(game.getBoardJson());
        int row = request.getRow();
        int col = request.getCol();
        if (board[row][col] != 0) {
            throw new BusinessException("该位置已有棋子");
        }
        boolean black = userId.equals(game.getBlackPlayerId());
        int stone = black ? 1 : 2;
        board[row][col] = stone;
        game.setBoardJson(toJson(board));
        game.setMoveCount(game.getMoveCount() + 1);

        Integer earned = null;
        if (GomokuRules.isWinningMove(board, row, col, stone)) {
            game.setStatus(black ? "BLACK_WON" : "WHITE_WON");
            game.setFinishReason("NORMAL");
            game.setWinnerId(userId);
            game.setCompletedAt(LocalDateTime.now());
            settle(game, userId, black ? game.getWhitePlayerId() : game.getBlackPlayerId());
            earned = WIN_POINTS;
        } else if (game.getMoveCount() == GomokuRules.BOARD_SIZE * GomokuRules.BOARD_SIZE) {
            game.setStatus("DRAW");
            game.setFinishReason("DRAW");
            game.setCompletedAt(LocalDateTime.now());
            awardDrawPoints(game);
            earned = LOSE_POINTS;
        } else {
            game.setCurrentPlayerId(black ? game.getWhitePlayerId() : game.getBlackPlayerId());
        }
        gameMapper.updateById(game);
        return toResponse(game, userId, earned);
    }

    /** 认输；等待中的房主可用同一操作取消房间且不结算。 */
    @Transactional
    public GomokuGameResponse surrender(Long userId, Long gameId) {
        GomokuGame game = lockGame(gameId);
        requireParticipant(game, userId);
        if ("WAITING".equals(game.getStatus())) {
            game.setStatus("CANCELLED");
            game.setFinishReason("CANCELLED");
            game.setCompletedAt(LocalDateTime.now());
            gameMapper.updateById(game);
            return toResponse(game, userId, 0);
        }
        if (!"IN_PROGRESS".equals(game.getStatus())) {
            throw new BusinessException("对局已经结束");
        }
        Long winnerId = userId.equals(game.getBlackPlayerId()) ? game.getWhitePlayerId() : game.getBlackPlayerId();
        boolean blackWon = winnerId.equals(game.getBlackPlayerId());
        game.setStatus(blackWon ? "BLACK_WON" : "WHITE_WON");
        game.setFinishReason("SURRENDER");
        game.setWinnerId(winnerId);
        game.setCompletedAt(LocalDateTime.now());
        gameMapper.updateById(game);
        return toResponse(game, userId, 0);
    }

    private void settle(GomokuGame game, Long winnerId, Long loserId) {
        pointsService.awardPoints(winnerId, WIN_POINTS, "GOMOKU_WIN", game.getId(), "五子棋获胜");
        pointsService.awardPoints(loserId, LOSE_POINTS, "GOMOKU_LOSE", game.getId(), "五子棋参与奖励");
    }

    private void awardDrawPoints(GomokuGame game) {
        pointsService.awardPoints(game.getBlackPlayerId(), LOSE_POINTS, "GOMOKU_DRAW", game.getId(), "五子棋平局奖励");
        pointsService.awardPoints(game.getWhitePlayerId(), LOSE_POINTS, "GOMOKU_DRAW", game.getId(), "五子棋平局奖励");
    }

    private GomokuGame lockGame(Long gameId) {
        GomokuGame game = gameMapper.selectOne(new LambdaQueryWrapper<GomokuGame>()
                .eq(GomokuGame::getId, gameId).last("FOR UPDATE"));
        if (game == null) {
            throw new BusinessException("对局不存在");
        }
        return game;
    }

    private GomokuGame requireParticipant(GomokuGame game, Long userId) {
        if (game == null || (!userId.equals(game.getBlackPlayerId()) && !userId.equals(game.getWhitePlayerId()))) {
            throw new BusinessException("对局不存在或你不是对局玩家");
        }
        return game;
    }

    private void ensureUserExists(Long userId) {
        if (userMapper.selectById(userId) == null) {
            throw new BusinessException("用户不存在");
        }
    }

    private void ensureNoActiveGame(Long userId) {
        if (findActiveGame(userId) != null) {
            throw new BusinessException("你已有未结束的五子棋对局");
        }
    }

    private GomokuGame findActiveGame(Long userId) {
        return gameMapper.selectOne(new LambdaQueryWrapper<GomokuGame>()
                .in(GomokuGame::getStatus, "WAITING", "IN_PROGRESS")
                .and(wrapper -> wrapper.eq(GomokuGame::getBlackPlayerId, userId)
                        .or().eq(GomokuGame::getWhitePlayerId, userId))
                .orderByDesc(GomokuGame::getCreatedAt)
                .last("LIMIT 1"));
    }

    private GomokuGameResponse toResponse(GomokuGame game, Long userId, Integer pointsEarned) {
        User black = userMapper.selectById(game.getBlackPlayerId());
        User white = game.getWhitePlayerId() == null ? null : userMapper.selectById(game.getWhitePlayerId());
        return GomokuGameResponse.builder()
                .id(game.getId()).roomCode(game.getRoomCode()).board(fromJson(game.getBoardJson()))
                .status(game.getStatus()).finishReason(game.getFinishReason()).moveCount(game.getMoveCount())
                .blackPlayerId(game.getBlackPlayerId()).blackPlayerName(black == null ? "未知玩家" : black.getNickname())
                .whitePlayerId(game.getWhitePlayerId()).whitePlayerName(white == null ? null : white.getNickname())
                .currentPlayerId(game.getCurrentPlayerId()).winnerId(game.getWinnerId())
                .myColor(userId.equals(game.getBlackPlayerId()) ? "BLACK" : "WHITE")
                .myTurn("IN_PROGRESS".equals(game.getStatus()) && userId.equals(game.getCurrentPlayerId()))
                .pointsEarned(pointsEarned).build();
    }

    private String normalizeRoomCode(String roomCode) {
        if (roomCode == null || roomCode.isBlank()) {
            throw new BusinessException("请输入房间码");
        }
        return roomCode.trim().toUpperCase(Locale.ROOT);
    }

    private String randomRoomCode() {
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder result = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            result.append(alphabet.charAt(ThreadLocalRandom.current().nextInt(alphabet.length())));
        }
        return result.toString();
    }

    private String toJson(int[][] board) {
        try {
            return objectMapper.writeValueAsString(board);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("棋盘序列化失败", e);
        }
    }

    private int[][] fromJson(String json) {
        try {
            return objectMapper.readValue(json, int[][].class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("棋盘数据损坏", e);
        }
    }
}
