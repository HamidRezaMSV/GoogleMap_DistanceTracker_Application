package com.sm.distancetracker.di


////@Module
////@InstallIn(ServiceComponent::class)
object NotificationModule {

////    @ServiceScoped
////    @Provides
//    fun providePendingIntent(@ApplicationContext context: Context) : PendingIntent{
//        return PendingIntent.getActivity(
//            context ,
//            PENDING_INTENT_REQUEST_CODE ,
//            Intent(context,MainActivity::class.java).apply {
//                this.action = ACTION_NAVIGATE_TO_MAPS_FRAGMENT
//            },
//            PendingIntent.FLAG_UPDATE_CURRENT
//        )
//    }
//
////    @ServiceScoped
////    @Provides
//    fun provideNotificationBuilder(
//        @ApplicationContext context: Context ,
//        pendingIntent: PendingIntent
//    ):NotificationCompat.Builder{
//        return NotificationCompat.Builder(context,NOTIFICATION_CHANNEL_ID)
//            .setAutoCancel(false)
//            .setOngoing(true)
//            .setSmallIcon(R.drawable.ic_run)
//            .setContentIntent(pendingIntent)
//    }
//
////    @ServiceScoped
////    @Provides
//    fun provideNotificationManager(@ApplicationContext context: Context) : NotificationManager{
//        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//    }
//
}