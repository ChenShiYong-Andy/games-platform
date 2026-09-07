package com.gamesplatform.games.gomoku.service;

import io.swagger.v3.oas.annotations.media.Schema;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamesplatform.common.BusinessException;
import com.gamesplatform.games.gomoku.domain.GomokuAi;
import com.gamesplatform.games.gomoku.domain.GomokuRules;
import com.gamesplatform.games.gomoku.dto.GomokuGameResponse;
import com.gamesplatform.games.gomoku.dto.MoveRequest;
import com.gamesplatform.games.gomoku.entity.GomokuGame;
import com.gamesplatform.games.gomoku.mapper.GomokuGameMapper;
import com.gamesplatform.games.domain.WaitingRoomResponse;
import com.gamesplatform.system.points.service.PointsService;
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

/** 双人五子棋业务服务。 */
@Service
@RequiredArgsConstructor
public class GomokuService {
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
     * 房间码最大生成尝试次数。
     */
    @Schema(description = "房间码最大生成尝试次数")
    private static final int MAX_ROOM_CODE_ATTEMPTS = 10;


    /**
     * 游戏数据访问组件。
     */
    @Schema(description = "游戏数据访问组件")
    private final GomokuGameMapper gameMapper;
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
            game.setGameMode("FRIEND");
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

    /**
     * 创建人机对局。
     *
     * @param userId 用户标识
     * @return 操作结果
     */
    @Transactional
    public GomokuGameResponse createAiGame(Long userId) {
        ensureUserExists(userId);
        ensureNoActiveGame(userId);
        GomokuGame game = new GomokuGame();
        game.setRoomCode(randomRoomCode());
        game.setBlackPlayerId(userId);
        game.setWhitePlayerId(userId);
        game.setCurrentPlayerId(userId);
        game.setMoveCount(0);
        game.setStatus("IN_PROGRESS");
        game.setGameMode("AI");
        boolean humanBlack = ThreadLocalRandom.current().nextBoolean();
        game.setHumanColor(humanBlack ? "BLACK" : "WHITE");
        int[][] board = new int[GomokuRules.BOARD_SIZE][GomokuRules.BOARD_SIZE];
        if (!humanBlack) {
            int[] aiMove = GomokuAi.chooseMove(board, 1);
            board[aiMove[0]][aiMove[1]] = 1;
            game.setMoveCount(1);
            game.setLastMoveRow(aiMove[0]);
            game.setLastMoveCol(aiMove[1]);
        }
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
     * @param rawRoomCode 原始房间码
     * @return 操作结果
     */
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
    public GomokuGameResponse getGame(Long userId, Long gameId) {
        return toResponse(requireParticipant(gameMapper.selectById(gameId), userId), userId, null);
    }

    /**
     * 查询当前用户未结束的对局。
     *
     * @param userId 用户标识
     * @return 操作结果
     */
    public GomokuGameResponse getActiveGame(Long userId) {
        GomokuGame game = findActiveGame(userId);
        return game == null ? null : toResponse(game, userId, null);
    }

    /**
     * 查询当前可加入的等待房间。
     *
     * @param userId 用户标识
     * @return 操作结果
     */
    public List<WaitingRoomResponse> getWaitingRooms(Long userId) {
        return gameMapper.selectList(new LambdaQueryWrapper<GomokuGame>()
                        .eq(GomokuGame::getStatus, "WAITING")
                        .eq(GomokuGame::getGameMode, "FRIEND")
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

    /**
     * 执行玩家走棋并返回最新对局状态。
     *
     * @param userId 用户标识
     * @param gameId 游戏标识
     * @param request 请求参数
     * @return 操作结果
     */
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
        boolean aiGame = isAi(game);
        boolean black = aiGame ? "BLACK".equals(game.getHumanColor()) : userId.equals(game.getBlackPlayerId());
        int stone = black ? 1 : 2;
        board[row][col] = stone;
        game.setMoveCount(game.getMoveCount() + 1);
        game.setLastMoveRow(row);
        game.setLastMoveCol(col);

        Integer earned = null;
        if (GomokuRules.isWinningMove(board, row, col, stone)) {
            game.setStatus(black ? "BLACK_WON" : "WHITE_WON");
            game.setFinishReason("NORMAL");
            game.setWinnerId(userId);
            game.setCompletedAt(LocalDateTime.now());
            if (aiGame) awardHuman(game, userId, WIN_POINTS, "GOMOKU_AI_WIN", "五子棋人机对局获胜");
            else settle(game, userId, black ? game.getWhitePlayerId() : game.getBlackPlayerId());
            earned = WIN_POINTS;
        } else if (game.getMoveCount() == GomokuRules.BOARD_SIZE * GomokuRules.BOARD_SIZE) {
            game.setStatus("DRAW");
            game.setFinishReason("DRAW");
            game.setCompletedAt(LocalDateTime.now());
            if (aiGame) awardHuman(game, userId, LOSE_POINTS, "GOMOKU_AI_DRAW", "五子棋人机对局平局奖励");
            else awardDrawPoints(game);
            earned = LOSE_POINTS;
        } else if (aiGame) {
            earned = performAiMove(game, board, userId, black);
        } else {
            game.setCurrentPlayerId(black ? game.getWhitePlayerId() : game.getBlackPlayerId());
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
        boolean aiGame = isAi(game);
        boolean humanBlack = aiGame ? "BLACK".equals(game.getHumanColor()) : userId.equals(game.getBlackPlayerId());
        Long winnerId = aiGame ? null : (humanBlack ? game.getWhitePlayerId() : game.getBlackPlayerId());
        boolean blackWon = aiGame ? !humanBlack : winnerId.equals(game.getBlackPlayerId());
        game.setStatus(blackWon ? "BLACK_WON" : "WHITE_WON");
        game.setFinishReason("SURRENDER");
        game.setWinnerId(winnerId);
        game.setCompletedAt(LocalDateTime.now());
        gameMapper.updateById(game);
        return toResponse(game, userId, 0);
    }

    /**
     * 执行电脑走棋并处理胜负结算。
     *
     * @param game 游戏记录
     * @param board 棋盘数据
     * @param userId 用户标识
     * @param humanBlack 玩家是否执黑方
     * @return 玩家获得的积分；未结算时返回 null
     */
    private Integer performAiMove(GomokuGame game, int[][] board, Long userId, boolean humanBlack) {
        int aiStone = humanBlack ? 2 : 1;
        int[] move = GomokuAi.chooseMove(board, aiStone);
        if (move == null) return null;
        board[move[0]][move[1]] = aiStone;
        game.setMoveCount(game.getMoveCount() + 1);
        game.setLastMoveRow(move[0]);
        game.setLastMoveCol(move[1]);
        if (GomokuRules.isWinningMove(board, move[0], move[1], aiStone)) {
            game.setStatus(aiStone == 1 ? "BLACK_WON" : "WHITE_WON");
            game.setFinishReason("NORMAL");
            game.setWinnerId(null);
            game.setCompletedAt(LocalDateTime.now());
            awardHuman(game, userId, LOSE_POINTS, "GOMOKU_AI_LOSE", "五子棋人机对局参与奖励");
            return LOSE_POINTS;
        }
        if (game.getMoveCount() == GomokuRules.BOARD_SIZE * GomokuRules.BOARD_SIZE) {
            game.setStatus("DRAW");
            game.setFinishReason("DRAW");
            game.setCompletedAt(LocalDateTime.now());
            awardHuman(game, userId, LOSE_POINTS, "GOMOKU_AI_DRAW", "五子棋人机对局平局奖励");
            return LOSE_POINTS;
        }
        game.setCurrentPlayerId(userId);
        return null;
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
    private void awardHuman(GomokuGame game, Long userId, int points, String type, String description) {
        pointsService.awardPoints(userId, points, type, game.getId(), description);
    }

    /**
     * 结算好友对局双方积分。
     *
     * @param game 游戏记录
     * @param winnerId 获胜玩家标识
     * @param loserId 失败玩家标识
     */
    private void settle(GomokuGame game, Long winnerId, Long loserId) {
        pointsService.awardPoints(winnerId, WIN_POINTS, "GOMOKU_WIN", game.getId(), "五子棋获胜");
        pointsService.awardPoints(loserId, LOSE_POINTS, "GOMOKU_LOSE", game.getId(), "五子棋参与奖励");
    }

    /**
     * 发放平局参与积分。
     *
     * @param game 游戏记录
     */
    private void awardDrawPoints(GomokuGame game) {
        pointsService.awardPoints(game.getBlackPlayerId(), LOSE_POINTS, "GOMOKU_DRAW", game.getId(), "五子棋平局奖励");
        pointsService.awardPoints(game.getWhitePlayerId(), LOSE_POINTS, "GOMOKU_DRAW", game.getId(), "五子棋平局奖励");
    }

    /**
     * 查询并锁定指定对局记录。
     *
     * @param gameId 游戏标识
     * @return 操作结果
     */
    private GomokuGame lockGame(Long gameId) {
        GomokuGame game = gameMapper.selectOne(new LambdaQueryWrapper<GomokuGame>()
                .eq(GomokuGame::getId, gameId).last("FOR UPDATE"));
        if (game == null) {
            throw new BusinessException("对局不存在");
        }
        return game;
    }

    /**
     * 校验用户是否为指定对局参与者。
     *
     * @param game 游戏记录
     * @param userId 用户标识
     * @return 操作结果
     */
    private GomokuGame requireParticipant(GomokuGame game, Long userId) {
        if (game == null || (!userId.equals(game.getBlackPlayerId()) && !userId.equals(game.getWhitePlayerId()))) {
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
        if (userMapper.selectById(userId) == null) {
            throw new BusinessException("用户不存在");
        }
    }

    /**
     * 校验用户当前没有未结束的对局。
     *
     * @param userId 用户标识
     */
    private void ensureNoActiveGame(Long userId) {
        if (findActiveGame(userId) != null) {
            throw new BusinessException("你已有未结束的五子棋对局");
        }
    }

    /**
     * 查询用户当前未结束的对局。
     *
     * @param userId 用户标识
     * @return 操作结果
     */
    private GomokuGame findActiveGame(Long userId) {
        return gameMapper.selectOne(new LambdaQueryWrapper<GomokuGame>()
                .in(GomokuGame::getStatus, "WAITING", "IN_PROGRESS")
                .and(wrapper -> wrapper.eq(GomokuGame::getBlackPlayerId, userId)
                        .or().eq(GomokuGame::getWhitePlayerId, userId))
                .orderByDesc(GomokuGame::getCreatedAt)
                .last("LIMIT 1"));
    }

    /**
     * 将对局实体转换为接口响应。
     *
     * @param game 游戏记录
     * @param userId 用户标识
     * @param pointsEarned 本次获得积分
     * @return 操作结果
     */
    private GomokuGameResponse toResponse(GomokuGame game, Long userId, Integer pointsEarned) {
        if (isAi(game)) {
            User human = userMapper.selectById(userId);
            boolean humanBlack = "BLACK".equals(game.getHumanColor());
            return GomokuGameResponse.builder()
                    .id(game.getId()).roomCode(game.getRoomCode()).board(fromJson(game.getBoardJson()))
                    .status(game.getStatus()).finishReason(game.getFinishReason()).gameMode("AI")
                    .moveCount(game.getMoveCount())
                    .lastMoveRow(game.getLastMoveRow()).lastMoveCol(game.getLastMoveCol())
                    .blackPlayerId(humanBlack ? userId : null)
                    .blackPlayerName(humanBlack ? human.getNickname() : "电脑")
                    .whitePlayerId(humanBlack ? null : userId)
                    .whitePlayerName(humanBlack ? "电脑" : human.getNickname())
                    .currentPlayerId(game.getCurrentPlayerId()).winnerId(game.getWinnerId())
                    .myColor(game.getHumanColor())
                    .myTurn("IN_PROGRESS".equals(game.getStatus()))
                    .pointsEarned(pointsEarned).build();
        }
        User black = userMapper.selectById(game.getBlackPlayerId());
        User white = game.getWhitePlayerId() == null ? null : userMapper.selectById(game.getWhitePlayerId());
        return GomokuGameResponse.builder()
                .id(game.getId()).roomCode(game.getRoomCode()).board(fromJson(game.getBoardJson()))
                .status(game.getStatus()).finishReason(game.getFinishReason()).gameMode("FRIEND").moveCount(game.getMoveCount())
                .lastMoveRow(game.getLastMoveRow()).lastMoveCol(game.getLastMoveCol())
                .blackPlayerId(game.getBlackPlayerId()).blackPlayerName(black == null ? "未知玩家" : black.getNickname())
                .whitePlayerId(game.getWhitePlayerId()).whitePlayerName(white == null ? null : white.getNickname())
                .currentPlayerId(game.getCurrentPlayerId()).winnerId(game.getWinnerId())
                .myColor(userId.equals(game.getBlackPlayerId()) ? "BLACK" : "WHITE")
                .myTurn("IN_PROGRESS".equals(game.getStatus()) && userId.equals(game.getCurrentPlayerId()))
                .pointsEarned(pointsEarned).build();
    }

    /**
     * 判断对局是否为人机模式。
     *
     * @param game 游戏记录
     * @return 满足条件时返回 true，否则返回 false
     */
    private boolean isAi(GomokuGame game) {
        return "AI".equals(game.getGameMode());
    }

    /**
     * 校验并规范化房间码。
     *
     * @param roomCode 房间码
     * @return 对应的文本结果
     */
    private String normalizeRoomCode(String roomCode) {
        if (roomCode == null || roomCode.isBlank()) {
            throw new BusinessException("请输入房间码");
        }
        return roomCode.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * 生成随机房间码。
     * @return 对应的文本结果
     */
    private String randomRoomCode() {
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder result = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            result.append(alphabet.charAt(ThreadLocalRandom.current().nextInt(alphabet.length())));
        }
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
