package br.com.queue.dtos.user.metrics;

import java.util.List;

public record ResponseUserDashBoardDto(

        ResponseCountTotalUsersStatisticsDto countTotalUsersStatistics,

        ResponseUserPercentagesStatisticsDto userPercentagesStatistics,

        List<ResponseUsersCreatedByMonthStatisticsDto> usersCreatedByMonthStatistics,

        List<ResponseServicesByUserStatisticsDto> countServicesByUsers,

        List<ResponseUsersByRoleStatisticsDto> countRoleByUsers



) {
}
