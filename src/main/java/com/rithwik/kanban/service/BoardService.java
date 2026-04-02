package com.rithwik.kanban.service;

import com.rithwik.kanban.dtos.allDto.BoardFullResponse;
import com.rithwik.kanban.dtos.boardDtos.BoardMemberResponse;
import com.rithwik.kanban.dtos.boardDtos.BoardResponse;
import com.rithwik.kanban.dtos.boardDtos.CreateBoardRequest;
import com.rithwik.kanban.dtos.boardDtos.UpdateBoardRequest;

import java.util.List;

public interface BoardService {

    BoardResponse createBoard(CreateBoardRequest request);

    List<BoardResponse> getBoardsForCurrentUser();

    BoardFullResponse getBoardFull(Long boardId);

    BoardResponse updateBoard(Long boardId, UpdateBoardRequest request);

    void deleteBoard(Long boardId);

    void inviteUser(Long boardId, String email);

    List<BoardMemberResponse> getBoardMembers(Long boardId);
}
