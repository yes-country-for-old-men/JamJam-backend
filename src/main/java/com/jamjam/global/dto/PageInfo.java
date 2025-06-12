package com.jamjam.global.dto;

public record PageInfo(
        int currentPage,
        int totalPage
) {
    public static PageInfo of(int currentPage, int totalPage) {
        return new PageInfo(currentPage, totalPage);
    }
}

