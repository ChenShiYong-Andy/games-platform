package com.gamesplatform.games.chess.service;

import io.swagger.v3.oas.annotations.media.Schema;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamesplatform.games.chess.domain.ChineseChessAi;
import com.gamesplatform.games.chess.domain.ChineseChessRules;
import com.gamesplatform.games.chess.dto.ChessMoveRequest;
import com.gamesplatform.games.chess.dto.ChineseChessGameResponse;
import com.gamesplatform.games.chess.entity.ChineseChessGame;
import com.gamesplatform.games.chess.mapper.ChineseChessGameMapper;
import com.gamesplatform.common.BusinessException;
import com.gamesplatform.system.points.service.PointsService;
import com.gamesplatform.games.domain.WaitingRoomResponse;
import com.gamesplatform.system.user.entity.User;
import com.gamesplatform.system.user.mapper.UserMapper;
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
    /**
     * 胜方奖励积分。
     */
    @Schema(description = "胜方奖励积分")
    private static final int WIN_POINTS = 10;
    /**
     * 败方参与奖励积分。
     */
    @Schema(description = "败方参与奖励积分")
    private static final int LOSE_POINTS = 5;
    /**
     * 游戏数据访问组件。
     */
    @Schema(description = "游戏数据访问组件")
    private final ChineseChessGameMapper gameMapper;
    /**
     * 用户数据访问组件。
     */
    @Schema(description = "用户数据访问组件")
    private final UserMapper userMapper;
    /**
     * 积分业务服务。
     */
    @Schema(description = "积分业务服务")
    private final PointsService pointsService;
    /**
     * JSON 序列化组件。
     */
    @Schema(description = "JSON 序列化组件")
    private final ObjectMapper objectMapper;


    /**
     * 创建好友对战邀请房间。
     *
     * @param userId 用户标识
     * @return 操作结果
     */
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

    /**
     * 创建人机对局。
     *
     * @param userId 用户标识
     * @return 操作结果
     */
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

    /**
     * 通过房间码加入好友对局。
     *
     * @param userId 用户标识
     * @param rawCode 原始房间码
     * @return 操作结果
     */
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

    /**
     * 查询指定游戏的最新状态。
     *
     * @param userId 用户标识
     * @param gameId 游戏标识
     * @return 操作结果
     */
    public ChineseChessGameResponse getGame(Long userId, Long gameId) {
        return toResponse(requireParticipant(gameMapper.selectById(gameId), userId), userId, null);
    }

    /**
     * 查询当前用户未结束的对局。
     *
     * @param userId 用户标识
     * @return 操作结果
     */
    public ChineseChessGameResponse getActiveGame(Long userId) {
        ChineseChessGame game = findActiveGame(userId);
        return game == null ? null : toResponse(game, userId, null);
    }

    /**
     * 查询当前可加入的等待房间。
     *
     * @param userId 用户标识
     * @return 操作结果
     */
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

    /**
     * 执行玩家走棋并返回最新对局状态。
     *
     * @param userId 用户标识
     * @param gameId 游戏标识
     * @param request 请求参数
     * @return 操作结果
     */
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

    /**
     * 认输或取消等待中的房间。
     *
     * @param userId 用户标识
     * @param gameId 游戏标识
     * @return 操作结果
     */
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

    /**
     * 执行电脑执先时的开局走法。
     *
     * @param game 游戏记录
     * @param board 棋盘数据
     */
    private void applyOpeningAiMove(ChineseChessGame game, int[][] board) {
        int[] move = ChineseChessAi.chooseMove(board, true);
        if (move == null) return;
        board[move[2]][move[3]] = board[move[0]][move[1]];
        board[move[0]][move[1]] = 0;
        game.setMoveCount(1);
        setLastMove(game, move);
    }

    /**
     * 执行电脑走棋并处理胜负结算。
     *
     * @param game 游戏记录
     * @param board 棋盘数据
     * @param userId 用户标识
     * @param aiRed 电脑是否执红方
     * @return 玩家获得的积分；未结算时返回 null
     */
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

    /**
     * 记录最近一步走棋坐标。
     *
     * @param game 游戏记录
     * @param move 走棋坐标
     */
    private void setLastMove(ChineseChessGame game, int[] move) {
        game.setLastFromRow(move[0]);
        game.setLastFromCol(move[1]);
        game.setLastToRow(move[2]);
        game.setLastToCol(move[3]);
    }

    /**
     * 为人机对局中的玩家发放积分。
     *
     * @param game 游戏记录
     * @param userId 用户标识
     * @param points 奖励积分
     * @param type 积分记录类型
     * @param description 积分记录说明
     */
    private void awardHuman(ChineseChessGame game, Long userId, int points, String type, String description) {
        pointsService.awardPoints(userId, points, type, game.getId(), description);
    }

    /**
     * 结算好友对局双方积分。
     *
     * @param game 游戏记录
     * @param winnerId 获胜玩家标识
     * @param loserId 失败玩家标识
     */
    private void settle(ChineseChessGame game, Long winnerId, Long loserId) {
        pointsService.awardPoints(winnerId, WIN_POINTS, "CHESS_WIN", game.getId(), "中国象棋获胜");
        pointsService.awardPoints(loserId, LOSE_POINTS, "CHESS_LOSE", game.getId(), "中国象棋参与奖励");
    }

    /**
     * 校验走棋坐标参数。
     *
     * @param request 请求参数
     */
    private void validateCoordinates(ChessMoveRequest request) {
        if (request == null || request.getFromRow() == null || request.getFromCol() == null
                || request.getToRow() == null || request.getToCol() == null
                || !inside(request.getFromRow(), request.getFromCol()) || !inside(request.getToRow(), request.getToCol())) {
            throw new BusinessException("走子位置无效");
        }
    }

    /**
     * 判断坐标是否位于棋盘范围内。
     *
     * @param row 行坐标
     * @param col 列坐标
     * @return 满足条件时返回 true，否则返回 false
     */
    private boolean inside(int row, int col) {
        return row >= 0 && row < ChineseChessRules.ROWS && col >= 0 && col < ChineseChessRules.COLS;
    }

    /**
     * 查询并锁定指定对局记录。
     *
     * @param gameId 游戏标识
     * @return 操作结果
     */
    private ChineseChessGame lockGame(Long gameId) {
        ChineseChessGame game = gameMapper.selectOne(new LambdaQueryWrapper<ChineseChessGame>()
                .eq(ChineseChessGame::getId, gameId).last("FOR UPDATE"));
        if (game == null) throw new BusinessException("对局不存在");
        return game;
    }

    /**
     * 校验用户是否为指定对局参与者。
     *
     * @param game 游戏记录
     * @param userId 用户标识
     * @return 操作结果
     */
    private ChineseChessGame requireParticipant(ChineseChessGame game, Long userId) {
        if (game == null || (!userId.equals(game.getRedPlayerId()) && !userId.equals(game.getBlackPlayerId()))) {
            throw new BusinessException("对局不存在或你不是对局玩家");
        }
        return game;
    }

    /**
     * 校验用户是否存在。
     *
     * @param userId 用户标识
     */
    private void ensureUserExists(Long userId) {
        if (userMapper.selectById(userId) == null) throw new BusinessException("用户不存在");
    }

    /**
     * 校验用户当前没有未结束的对局。
     *
     * @param userId 用户标识
     */
    private void ensureNoActiveGame(Long userId) {
        if (findActiveGame(userId) != null) throw new BusinessException("你已有未结束的象棋对局");
    }

    /**
     * 查询用户当前未结束的对局。
     *
     * @param userId 用户标识
     * @return 操作结果
     */
    private ChineseChessGame findActiveGame(Long userId) {
        return gameMapper.selectOne(new LambdaQueryWrapper<ChineseChessGame>()
                .in(ChineseChessGame::getStatus, "WAITING", "IN_PROGRESS")
                .and(w -> w.eq(ChineseChessGame::getRedPlayerId, userId)
                        .or().eq(ChineseChessGame::getBlackPlayerId, userId))
                .orderByDesc(ChineseChessGame::getCreatedAt).last("LIMIT 1"));
    }

    /**
     * 将对局实体转换为接口响应。
     *
     * @param game 游戏记录
     * @param userId 用户标识
     * @param pointsEarned 本次获得积分
     * @return 操作结果
     */
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

    /**
     * 判断对局是否为人机模式。
     *
     * @param game 游戏记录
     * @return 满足条件时返回 true，否则返回 false
     */
    private boolean isAi(ChineseChessGame game) {
        return "AI".equals(game.getGameMode());
    }

    /**
     * 校验并规范化房间码。
     *
     * @param code 房间码
     * @return 对应的文本结果
     */
    private String normalizeRoomCode(String code) {
        if (code == null || code.isBlank()) throw new BusinessException("请输入房间码");
        return code.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * 生成随机房间码。
     * @return 对应的文本结果
     */
    private String randomRoomCode() {
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder result = new StringBuilder(6);
        for (int i = 0; i < 6; i++) result.append(alphabet.charAt(ThreadLocalRandom.current().nextInt(alphabet.length())));
        return result.toString();
    }

    /**
     * 将棋盘序列化为 JSON。
     *
     * @param board 棋盘数据
     * @return 对应的文本结果
     */
    private String toJson(int[][] board) {
        try {
            return objectMapper.writeValueAsString(board);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("棋盘序列化失败", e);
        }
    }

    /**
     * 将 JSON 反序列化为棋盘。
     *
     * @param json 棋盘 JSON 数据
     * @return 二维棋盘数据
     */
    private int[][] fromJson(String json) {
        try {
            return objectMapper.readValue(json, int[][].class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("棋盘数据损坏", e);
        }
    }
}
