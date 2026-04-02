package com.rithwik.kanban.controller;

import com.rithwik.kanban.dtos.allDto.BoardFullResponse;
import com.rithwik.kanban.dtos.boardDtos.*;
import com.rithwik.kanban.service.BoardService;
import com.rithwik.kanban.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @PostMapping
    public ResponseEntity<ApiResponse<BoardResponse>> createBoard(
            @RequestBody CreateBoardRequest request) {

        BoardResponse board = boardService.createBoard(request);
        ApiResponse<BoardResponse> response =
                new ApiResponse<>(true, "Board created successfully", board);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BoardResponse>>> getBoards() {

        List<BoardResponse> boards = boardService.getBoardsForCurrentUser();
        ApiResponse<List<BoardResponse>> response =
                new ApiResponse<>(true, "Boards fetched successfully", boards);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{boardId}/full")
    public ResponseEntity<ApiResponse<BoardFullResponse>> getBoardFull(
            @PathVariable Long boardId) {

        BoardFullResponse board = boardService.getBoardFull(boardId);
        ApiResponse<BoardFullResponse> response =
                new ApiResponse<>(true, "Board fetched successfully", board);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{boardId}")
    public ResponseEntity<ApiResponse<BoardResponse>> updateBoard(
            @PathVariable Long boardId,
            @RequestBody UpdateBoardRequest request) {

        BoardResponse board = boardService.updateBoard(boardId, request);
        ApiResponse<BoardResponse> response =
                new ApiResponse<>(true, "Board updated successfully", board);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<ApiResponse<Void>> deleteBoard(
            @PathVariable Long boardId) {

        boardService.deleteBoard(boardId);
        ApiResponse<Void> response =
                new ApiResponse<>(true, "Board deleted successfully", null);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{boardId}/invite")
    public ResponseEntity<ApiResponse<Void>> inviteUser(
            @PathVariable Long boardId,
            @RequestBody InviteRequest request) {

        boardService.inviteUser(boardId, request.getEmail());
        ApiResponse<Void> response =
                new ApiResponse<>(true, "User invited successfully", null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{boardId}/members")
    public ResponseEntity<ApiResponse<List<BoardMemberResponse>>> getBoardMembers(
            @PathVariable Long boardId) {

        List<BoardMemberResponse> members = boardService.getBoardMembers(boardId);

        ApiResponse<List<BoardMemberResponse>> response =
                new ApiResponse<>(true, "Members fetched successfully", members);

        return ResponseEntity.ok(response);
    }
}
