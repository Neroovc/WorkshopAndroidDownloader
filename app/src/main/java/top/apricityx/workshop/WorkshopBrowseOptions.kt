package top.apricityx.workshop

import android.content.Context

enum class WorkshopBrowseSortOption(
    val browseSortValue: String,
    val actualSortValue: String,
    val supportsTimeWindow: Boolean,
) {
    MostPopular(
        browseSortValue = "trend",
        actualSortValue = "trend",
        supportsTimeWindow = true,
    ),
    MostRecent(
        browseSortValue = "mostrecent",
        actualSortValue = "mostrecent",
        supportsTimeWindow = false,
    ),
    LastUpdated(
        browseSortValue = "lastupdated",
        actualSortValue = "lastupdated",
        supportsTimeWindow = false,
    ),
    MostSubscribed(
        browseSortValue = "totaluniquesubscribers",
        actualSortValue = "totaluniquesubscribers",
        supportsTimeWindow = false,
    ),
}

enum class WorkshopBrowseTimeWindow(
    val daysValue: Int,
) {
    Today(1),
    OneWeek(7),
    ThirtyDays(30),
    ThreeMonths(90),
    SixMonths(180),
    OneYear(365),
    AllTime(-1),
}

fun WorkshopBrowseSortOption.displayName(context: Context): String =
    when (this) {
        WorkshopBrowseSortOption.MostPopular -> context.getString(R.string.sort_popular)
        WorkshopBrowseSortOption.MostRecent -> context.getString(R.string.sort_recent)
        WorkshopBrowseSortOption.LastUpdated -> context.getString(R.string.sort_updated)
        WorkshopBrowseSortOption.MostSubscribed -> context.getString(R.string.sort_most_subscribed)
    }

fun WorkshopBrowseTimeWindow.displayName(context: Context): String =
    when (this) {
        WorkshopBrowseTimeWindow.Today -> context.getString(R.string.time_today)
        WorkshopBrowseTimeWindow.OneWeek -> context.getString(R.string.time_week)
        WorkshopBrowseTimeWindow.ThirtyDays -> context.getString(R.string.time_30d)
        WorkshopBrowseTimeWindow.ThreeMonths -> context.getString(R.string.time_3mo)
        WorkshopBrowseTimeWindow.SixMonths -> context.getString(R.string.time_6mo)
        WorkshopBrowseTimeWindow.OneYear -> context.getString(R.string.time_1y)
        WorkshopBrowseTimeWindow.AllTime -> context.getString(R.string.time_all)
    }
