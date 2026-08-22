import { CommentModule } from "@nabarun-ngo/nestjs-shared-comment";

export const COMMENT_MODULE = CommentModule.forRoot({
    allowedEntityTypes: [
        {
            entityType: 'donation',
            readPermissions: ['read:donation_comments'],
            writePermissions: ['create:donation_comments'],
        },
        {
            entityType: 'task',
            readPermissions: ['read:task_comments'],
            writePermissions: ['create:task_comments'],
        },
        {
            entityType: 'announcement',
        },
    ],
    notifications: {
        notifySubscribers: true,
    },
});