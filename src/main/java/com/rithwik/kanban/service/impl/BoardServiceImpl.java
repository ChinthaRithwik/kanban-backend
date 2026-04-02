package com.rithwik.kanban.service.impl;

import com.rithwik.kanban.dtos.allDto.BoardFullResponse;
import com.rithwik.kanban.dtos.allDto.ColumnWithTasksResponse;
import com.rithwik.kanban.dtos.boardDtos.BoardMemberResponse;
import com.rithwik.kanban.dtos.boardDtos.BoardResponse;
import com.rithwik.kanban.dtos.boardDtos.CreateBoardRequest;
import com.rithwik.kanban.dtos.boardDtos.UpdateBoardRequest;
import com.rithwik.kanban.dtos.taskDtos.TaskResponse;
import com.rithwik.kanban.entity.Board;
import com.rithwik.kanban.entity.BoardColumn;
import com.rithwik.kanban.entity.BoardMember;
import com.rithwik.kanban.entity.Role;
import com.rithwik.kanban.entity.Task;
import com.rithwik.kanban.entity.User;
import com.rithwik.kanban.exception.ResourceNotFoundException;
import com.rithwik.kanban.exception.UnauthorizedException;
import com.rithwik.kanban.repository.BoardMemberRepository;
import com.rithwik.kanban.repository.BoardRepository;
import com.rithwik.kanban.repository.ColumnRepository;
import com.rithwik.kanban.repository.TaskRepository;
import com.rithwik.kanban.repository.UserRepository;
import com.rithwik.kanban.service.BoardMemberService;
import com.rithwik.kanban.service.BoardService;
import com.rithwik.kanban.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final ColumnRepository columnRepository;
    private final TaskRepository taskRepository;
    private final BoardMemberRepository boardMemberRepository;
    private final BoardMemberService boardMemberService;

    @Override
    @Transactional
    public BoardResponse createBoard(CreateBoardRequest request) {
        String email = CurrentUser.getEmail();
        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Board board = Board.builder()
                .name(request.getName())
                .owner(owner)
                .createdAt(LocalDateTime.now())
                .build();

        Board saved = boardRepository.save(board);

        // Add the owner as an ADMIN member of the board so they can access it
        boardMemberService.addMember(saved, owner, Role.ADMIN);

        return BoardResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .createdAt(saved.getCreatedAt())
                .ownerId(owner.getId())
                .build();
    }

    @Override
    public List<BoardResponse> getBoardsForCurrentUser() {
        String email = CurrentUser.getEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return boardMemberRepository.findByUserId(user.getId()).stream()
                .map(BoardMember::getBoard)
                .distinct()
                .map(b -> BoardResponse.builder()
                        .id(b.getId())
                        .name(b.getName())
                        .createdAt(b.getCreatedAt())
                        .ownerId(b.getOwner().getId())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public BoardFullResponse getBoardFull(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        String email = CurrentUser.getEmail();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isMember = boardMemberRepository
                .findByBoardIdAndUserId(boardId, currentUser.getId())
                .isPresent();

        if (!isMember) {
            throw new UnauthorizedException("You are not allowed to access this board");
        }

        List<BoardColumn> columns = columnRepository.findByBoardIdOrderByPosition(boardId);

        List<ColumnWithTasksResponse> columnResponses = columns.stream().map(col -> {
            List<Task> tasks = taskRepository.findByColumnIdOrderByPosition(col.getId());

            List<TaskResponse> taskResponses = tasks.stream().map(t -> {
                TaskResponse tr = new TaskResponse();
                tr.setId(t.getId());
                tr.setTitle(t.getTitle());
                tr.setDescription(t.getDescription());
                tr.setPosition(t.getPosition());
                return tr;
            }).collect(Collectors.toList());

            ColumnWithTasksResponse cwr = new ColumnWithTasksResponse();
            cwr.setId(col.getId());
            cwr.setName(col.getName());
            cwr.setPosition(col.getPosition());
            cwr.setTasks(taskResponses);
            return cwr;
        }).collect(Collectors.toList());

        BoardFullResponse response = new BoardFullResponse();
        response.setId(board.getId());
        response.setName(board.getName());
        response.setColumns(columnResponses);
        return response;
    }

    @Override
    @Transactional
    public BoardResponse updateBoard(Long boardId, UpdateBoardRequest request) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        String email = CurrentUser.getEmail();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        BoardMember membership = boardMemberRepository
                .findByBoardIdAndUserId(boardId, currentUser.getId())
                .orElseThrow(() -> new UnauthorizedException("Only a board ADMIN can update the board"));

        if (membership.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Only a board ADMIN can update the board");
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            board.setName(request.getName());
        }

        Board saved = boardRepository.save(board);

        return BoardResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void deleteBoard(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        String email = CurrentUser.getEmail();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        BoardMember membership = boardMemberRepository
                .findByBoardIdAndUserId(boardId, currentUser.getId())
                .orElseThrow(() -> new UnauthorizedException("Only a board ADMIN can delete the board"));

        if (membership.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Only a board ADMIN can delete the board");
        }

        boardRepository.delete(board);
    }

    @Override
    @Transactional
    public void inviteUser(Long boardId, String email) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        // Verify the requesting user is an ADMIN of this board
        String currentEmail = CurrentUser.getEmail();
        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        BoardMember callerMembership = boardMemberRepository
                .findByBoardIdAndUserId(boardId, currentUser.getId())
                .orElseThrow(() -> new UnauthorizedException("Only a board ADMIN can invite members"));

        if (callerMembership.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Only a board ADMIN can invite members");
        }

        // Fetch the target user by email
        User targetUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email '" + email + "' not found"));

        // addMember handles duplicate-membership guard internally
        boardMemberService.addMember(board, targetUser, Role.MEMBER);
    }

    @Override
    public List<BoardMemberResponse> getBoardMembers(Long boardId) {

        // 1. Check board exists
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        // 2. Get current user
        String email = CurrentUser.getEmail();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 3. Check membership (IMPORTANT SECURITY)
        boolean isMember = boardMemberRepository
                .findByBoardIdAndUserId(boardId, currentUser.getId())
                .isPresent();

        if (!isMember) {
            throw new UnauthorizedException("You are not a member of this board");
        }

        // 4. Fetch all members
        List<BoardMember> members = boardMemberRepository.findByBoardId(boardId);

        // 5. Convert to DTO
        return members.stream().map(m -> new BoardMemberResponse(
                m.getUser().getId(),
                m.getUser().getName(),
                m.getUser().getEmail(),
                m.getRole().name()
        )).toList();
    }
}
