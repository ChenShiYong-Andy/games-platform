package com.gamesplatform.chess.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamesplatform.chess.domain.ChineseChessAi;
import com.gamesplatform.chess.domain.ChineseChessRules;
import com.gamesplatform.chess.dto.ChessMoveRequest;
import com.gamesplatform.chess.dto.ChineseChessGameResponse;
import com.gamesplatform.chess.entity.ChineseChessGame;
import com.gamesplatform.chess.mapper.ChineseChessGameMapper;
import com.gamesplatform.common.BusinessException;
import com.gamesplatform.points.service.PointsService;
import com.gamesplatform.game.domain.WaitingRoomResponse;
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

/** 双人中国象棋业务服务。 */
@Service
@RequiredArgsConstructor
public class ChineseChessService {
    private static final int WIN_POINTS = 10;
    private static final int LOSE_POINTS = 5;
    private final ChineseChessGameMapper gameMapper;
    private final UserMapper userMapper;
    private final PointsService pointsService;
    private final ObjectMapper objectMapper;

    @Transactional
    public ChineseChessGameResponse createRoom(Long userId) {
        ensureUserExists(userId);
        ensureNoActiveGame(userId);
        for (int attempt = 0; attempt < 10; attempt++) {
            ChineseChessGame game = new ChineseChessGame();
            game.setRoomCode(randomRoomCode());
            game.setRedPlayerId(userId);
            game.setCurrentPlayerId(userId);
            game.setBoardJson(toJson(ChineseChessRules.initialBoard()));
            game.setMoveCount(0);
            game.setStatus("WAITING");
            game.setGameMode("FRIEND");
            game.setCreatedAt(LocalDateTime.now());
            try {
                gameMapper.insert(game);
                return toResponse(game, userId, null);
            } catch (DuplicateKeyException ignored) {
                // 房间码冲突时重新生成。
            }
        }
        throw new BusinessException("房间创建失败，请稍后重试");
    }

    /** 创建人机对局，随机分配红黑方；电脑执红时自动走第一手。 */
    @Transactional
    public ChineseChessGameResponse createAiGame(Long userId) {
        ensureUserExists(userId);
        ensureNoActiveGame(userId);
        ChineseChessGame game = new ChineseChessGame();
        game.setRoomCode(randomRoomCode());
        game.setRedPlayerId(userId);
        game.setBlackPlayerId(userId);
        game.setCurrentPlayerId(userId);
        game.setMoveCount(0);
        game.setStatus("IN_PROGRESS");
        game.setGameMode("AI");
        boolean humanRed = ThreadLocalRandom.current().nextBoolean();
        game.setHumanColor(humanRed ? "RED" : "BLACK");
        int[][] board = ChineseChessRules.initialBoard();
        if (!humanRed) applyOpeningAiMove(game, board);
        game.setBoardJson(toJson(board));
        game.setCreatedAt(LocalDateTime.now());
        game.setStartedAt(LocalDateTime.now());
        gameMapper.insert(game);
        return toResponse(game, userId, null);
    }

    @Transactional
    public ChineseChessGameResponse joinRoom(Long userId, String rawCode) {
        String roomCode = normalizeRoomCode(rawCode);
        ChineseChessGame game = gameMapper.selectOne(new LambdaQueryWrapper<ChineseChessGame>()
                .eq(ChineseChessGame::getRoomCode, roomCode).last("FOR UPDATE"));
        if (game == null) throw new BusinessException("房间不存在");
        if (userId.equals(game.getRedPlayerId())) throw new BusinessException("不能加入自己创建的房间");
        if (!"WAITING".equals(game.getStatus()) || game.getBlackPlayerId() != null) {
            throw new BusinessException("房间已满或对局已开始");
        }
        ensureUserExists(userId);
        ensureNoActiveGame(userId);
        Long creatorId = game.getRedPlayerId();
        boolean creatorPlaysRed = ThreadLocalRandom.current().nextBoolean();
        if (creatorPlaysRed) {
            game.setBlackPlayerId(userId);
            game.setCurrentPlayerId(creatorId);
        } else {
            game.setRedPlayerId(userId);
            game.setBlackPlayerId(creatorId);
            game.setCurrentPlayerId(userId);
        }
        game.setStatus("IN_PROGRESS");
        game.setGameMode("FRIEND");
        game.setStartedAt(LocalDateTime.now());
        gameMapper.updateById(game);
        return toResponse(game, userId, null);
    }

    public ChineseChessGameResponse getGame(Long userId, Long gameId) {
        return toResponse(requireParticipant(gameMapper.selectById(gameId), userId), userId, null);
    }

    public ChineseChessGameResponse getActiveGame(Long userId) {
        ChineseChessGame game = findActiveGame(userId);
        return game == null ? null : toResponse(game, userId, null);
    }

    /** 查询可加入的等待中房间，不返回当前用户自己创建的房间。 */
    public List<WaitingRoomResponse> getWaitingRooms(Long userId) {
        return gameMapper.selectList(new LambdaQueryWrapper<ChineseChessGame>()
                        .eq(ChineseChessGame::getStatus, "WAITING")
                        .eq(ChineseChessGame::getGameMode, "FRIEND")
                        .ne(ChineseChessGame::getRedPlayerId, userId)
                        .orderByAsc(ChineseChessGame::getCreatedAt)
                        .last("LIMIT 50"))
                .stream()
                .map(game -> {
                    User host = userMapper.selectById(game.getRedPlayerId());
                    return WaitingRoomResponse.builder()
                            .id(game.getId())
                            .roomCode(game.getRoomCode())
                            .hostName(host == null ? "未知玩家" : host.getNickname())
                            .hostUsername(host == null ? "未知用户" : host.getUsername())
                            .build();
                }).toList();
    }

    @Transactional
    public ChineseChessGameResponse move(Long userId, Long gameId, ChessMoveRequest request) {
        ChineseChessGame game = lockGame(gameId);
        requireParticipant(game, userId);
        if (!"IN_PROGRESS".equals(game.getStatus())) throw new BusinessException("对局尚未开始或已经结束");
        if (!userId.equals(game.getCurrentPlayerId())) throw new BusinessException("还没有轮到你走棋");
        validateCoordinates(request);

        int[][] board = fromJson(game.getBoardJson());
        boolean aiGame = isAi(game);
        boolean red = aiGame ? "RED".equals(game.getHumanColor()) : userId.equals(game.getRedPlayerId());
        int fr = request.getFromRow(), fc = request.getFromCol(), tr = request.getToRow(), tc = request.getToCol();
        if (!ChineseChessRules.isLegalMove(board, fr, fc, tr, tc, red)) throw new BusinessException("该走法不符合象棋规则");
        int captured = board[tr][tc];
        board[tr][tc] = board[fr][fc];
        board[fr][fc] = 0;
        game.setMoveCount(game.getMoveCount() + 1);
        game.setLastFromRow(fr);
        game.setLastFromCol(fc);
        game.setLastToRow(tr);
        game.setLastToCol(tc);

        Long opponentId = red ? game.getBlackPlayerId() : game.getRedPlayerId();
        boolean capturedGeneral = Math.abs(captured) == ChineseChessRules.GENERAL;
        boolean opponentHasMove = !capturedGeneral && ChineseChessRules.hasLegalMove(board, !red);
        Integer earned = null;
        if (capturedGeneral || !opponentHasMove) {
            game.setStatus(red ? "RED_WON" : "BLACK_WON");
            game.setFinishReason("NORMAL");
            game.setWinnerId(userId);
            game.setCompletedAt(LocalDateTime.now());
            if (aiGame) awardHuman(game, userId, WIN_POINTS, "CHESS_AI_WIN", "中国象棋人机对局获胜");
            else settle(game, userId, opponentId);
            earned = WIN_POINTS;
        } else if (aiGame) {
            earned = performAiMove(game, board, userId, !red);
        } else {
            game.setCurrentPlayerId(opponentId);
        }
        game.setBoardJson(toJson(board));
        gameMapper.updateById(game);
        return toResponse(game, userId, earned);
    }

    @Transactional
    public ChineseChessGameResponse surrender(Long userId, Long gameId) {
        ChineseChessGame game = lockGame(gameId);
        requireParticipant(game, userId);
        if ("WAITING".equals(game.getStatus())) {
            game.setStatus("CANCELLED");
            game.setFinishReason("CANCELLED");
            game.setCompletedAt(LocalDateTime.now());
            gameMapper.updateById(game);
            return toResponse(game, userId, 0);
        }
        if (!"IN_PROGRESS".equals(game.getStatus())) throw new BusinessException("对局已经结束");
        boolean aiGame = isAi(game);
        boolean humanRed = aiGame ? "RED".equals(game.getHumanColor()) : userId.equals(game.getRedPlayerId());
        Long winnerId = aiGame ? null : (humanRed ? game.getBlackPlayerId() : game.getRedPlayerId());
        game.setStatus(aiGame ? (humanRed ? "BLACK_WON" : "RED_WON")
                : (winnerId.equals(game.getRedPlayerId()) ? "RED_WON" : "BLACK_WON"));
        game.setFinishReason("SURRENDER");
        game.setWinnerId(winnerId);
        game.setCompletedAt(LocalDateTime.now());
        gameMapper.updateById(game);
        return toResponse(game, userId, 0);
    }

    private void applyOpeningAiMove(ChineseChessGame game, int[][] board) {
        int[] move = ChineseChessAi.chooseMove(board, true);
        if (move == null) return;
        board[move[2]][move[3]] = board[move[0]][move[1]];
        board[move[0]][move[1]] = 0;
        game.setMoveCount(1);
        setLastMove(game, move);
    }

    private Integer performAiMove(ChineseChessGame game, int[][] board, Long userId, boolean aiRed) {
        int[] move = ChineseChessAi.chooseMove(board, aiRed);
        if (move == null) {
            game.setStatus(aiRed ? "BLACK_WON" : "RED_WON");
            game.setWinnerId(userId);
            game.setFinishReason("NORMAL");
            game.setCompletedAt(LocalDateTime.now());
            awardHuman(game, userId, WIN_POINTS, "CHESS_AI_WIN", "中国象棋人机对局获胜");
            return WIN_POINTS;
        }
        int captured = board[move[2]][move[3]];
        board[move[2]][move[3]] = board[move[0]][move[1]];
        board[move[0]][move[1]] = 0;
        game.setMoveCount(game.getMoveCount() + 1);
        setLastMove(game, move);
        boolean capturedGeneral = Math.abs(captured) == ChineseChessRules.GENERAL;
        boolean humanHasMove = !capturedGeneral && ChineseChessRules.hasLegalMove(board, !aiRed);
        if (capturedGeneral || !humanHasMove) {
            game.setStatus(aiRed ? "RED_WON" : "BLACK_WON");
            game.setWinnerId(null);
            game.setFinishReason("NORMAL");
            game.setCompletedAt(LocalDateTime.now());
            awardHuman(game, userId, LOSE_POINTS, "CHESS_AI_LOSE", "中国象棋人机对局参与奖励");
            return LOSE_POINTS;
        }
        game.setCurrentPlayerId(userId);
        return null;
    }

    private void setLastMove(ChineseChessGame game, int[] move) {
        game.setLastFromRow(move[0]);
        game.setLastFromCol(move[1]);
        game.setLastToRow(move[2]);
        game.setLastToCol(move[3]);
    }

    private void awardHuman(ChineseChessGame game, Long userId, int points, String type, String description) {
        pointsService.awardPoints(userId, points, type, game.getId(), description);
    }

    private void settle(ChineseChessGame game, Long winnerId, Long loserId) {
        pointsService.awardPoints(winnerId, WIN_POINTS, "CHESS_WIN", game.getId(), "中国象棋获胜");
        pointsService.awardPoints(loserId, LOSE_POINTS, "CHESS_LOSE", game.getId(), "中国象棋参与奖励");
    }

    private void validateCoordinates(ChessMoveRequest request) {
        if (request == null || request.getFromRow() == null || request.getFromCol() == null
                || request.getToRow() == null || request.getToCol() == null
                || !inside(request.getFromRow(), request.getFromCol()) || !inside(request.getToRow(), request.getToCol())) {
            throw new BusinessException("走子位置无效");
        }
    }

    private boolean inside(int row, int col) {
        return row >= 0 && row < ChineseChessRules.ROWS && col >= 0 && col < ChineseChessRules.COLS;
    }

    private ChineseChessGame lockGame(Long gameId) {
        ChineseChessGame game = gameMapper.selectOne(new LambdaQueryWrapper<ChineseChessGame>()
                .eq(ChineseChessGame::getId, gameId).last("FOR UPDATE"));
        if (game == null) throw new BusinessException("对局不存在");
        return game;
    }

    private ChineseChessGame requireParticipant(ChineseChessGame game, Long userId) {
        if (game == null || (!userId.equals(game.getRedPlayerId()) && !userId.equals(game.getBlackPlayerId()))) {
            throw new BusinessException("对局不存在或你不是对局玩家");
        }
        return game;
    }

    private void ensureUserExists(Long userId) {
        if (userMapper.selectById(userId) == null) throw new BusinessException("用户不存在");
    }

    private void ensureNoActiveGame(Long userId) {
        if (findActiveGame(userId) != null) throw new BusinessException("你已有未结束的象棋对局");
    }

    private ChineseChessGame findActiveGame(Long userId) {
        return gameMapper.selectOne(new LambdaQueryWrapper<ChineseChessGame>()
                .in(ChineseChessGame::getStatus, "WAITING", "IN_PROGRESS")
                .and(w -> w.eq(ChineseChessGame::getRedPlayerId, userId)
                        .or().eq(ChineseChessGame::getBlackPlayerId, userId))
                .orderByDesc(ChineseChessGame::getCreatedAt).last("LIMIT 1"));
    }

    private ChineseChessGameResponse toResponse(ChineseChessGame game, Long userId, Integer pointsEarned) {
        if (isAi(game)) {
            User human = userMapper.selectById(userId);
            boolean humanRed = "RED".equals(game.getHumanColor());
            int[][] board = fromJson(game.getBoardJson());
            boolean inCheck = "IN_PROGRESS".equals(game.getStatus())
                    && ChineseChessRules.isInCheck(board, humanRed);
            return ChineseChessGameResponse.builder()
                    .id(game.getId()).roomCode(game.getRoomCode()).board(board)
                    .status(game.getStatus()).finishReason(game.getFinishReason()).gameMode("AI")
                    .moveCount(game.getMoveCount())
                    .redPlayerId(humanRed ? userId : null).redPlayerName(humanRed ? human.getNickname() : "电脑")
                    .blackPlayerId(humanRed ? null : userId).blackPlayerName(humanRed ? "电脑" : human.getNickname())
                    .currentPlayerId(game.getCurrentPlayerId()).winnerId(game.getWinnerId())
                    .myColor(game.getHumanColor()).myTurn("IN_PROGRESS".equals(game.getStatus()))
                    .inCheck(inCheck)
                    .lastFromRow(game.getLastFromRow()).lastFromCol(game.getLastFromCol())
                    .lastToRow(game.getLastToRow()).lastToCol(game.getLastToCol())
                    .pointsEarned(pointsEarned).build();
        }
        User red = userMapper.selectById(game.getRedPlayerId());
        User black = game.getBlackPlayerId() == null ? null : userMapper.selectById(game.getBlackPlayerId());
        int[][] board = fromJson(game.getBoardJson());
        boolean inCheck = "IN_PROGRESS".equals(game.getStatus())
                && ChineseChessRules.isInCheck(board, game.getCurrentPlayerId().equals(game.getRedPlayerId()));
        return ChineseChessGameResponse.builder()
                .id(game.getId()).roomCode(game.getRoomCode()).board(board)
                .status(game.getStatus()).finishReason(game.getFinishReason()).gameMode("FRIEND").moveCount(game.getMoveCount())
                .redPlayerId(game.getRedPlayerId()).redPlayerName(red == null ? "未知玩家" : red.getNickname())
                .blackPlayerId(game.getBlackPlayerId()).blackPlayerName(black == null ? null : black.getNickname())
                .currentPlayerId(game.getCurrentPlayerId()).winnerId(game.getWinnerId())
                .myColor(userId.equals(game.getRedPlayerId()) ? "RED" : "BLACK")
                .myTurn("IN_PROGRESS".equals(game.getStatus()) && userId.equals(game.getCurrentPlayerId()))
                .inCheck(inCheck)
                .lastFromRow(game.getLastFromRow()).lastFromCol(game.getLastFromCol())
                .lastToRow(game.getLastToRow()).lastToCol(game.getLastToCol())
                .pointsEarned(pointsEarned).build();
    }

    private boolean isAi(ChineseChessGame game) {
        return "AI".equals(game.getGameMode());
    }

    private String normalizeRoomCode(String code) {
        if (code == null || code.isBlank()) throw new BusinessException("请输入房间码");
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private String randomRoomCode() {
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder result = new StringBuilder(6);
        for (int i = 0; i < 6; i++) result.append(alphabet.charAt(ThreadLocalRandom.current().nextInt(alphabet.length())));
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
