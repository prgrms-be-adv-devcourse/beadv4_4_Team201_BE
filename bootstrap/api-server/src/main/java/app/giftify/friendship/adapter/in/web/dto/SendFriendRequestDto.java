package app.giftify.friendship.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;

public record SendFriendRequestDto(
        @NotNull Long receiverId
) {}
