
package com.lewa.lockscreen.v5.lockscreen;

import com.lewa.lockscreen.laml.data.VariableBinderManager;
import com.lewa.lockscreen.laml.data.VariableNames;

public class BuiltinVariableBinders {

    public static void fill(VariableBinderManager m) {
        fillMissedCall(m);
        fillUnreadSms(m);
        fillUnreadMms(m);
    }

    private static void fillMissedCall(VariableBinderManager m) {
        String columns[] = {
                "_id", "number"
        };
        String where = "type=3 AND new=1";
        m.addContentProviderBinder("content://call_log/calls").setColumns(columns).setWhere(where)
                .setCountName(VariableNames.CALL_MISSED_COUNT);
    }

    private static void fillUnreadSms(VariableBinderManager m) {
        String columns[] = {
            "_id"
        };
        m.addContentProviderBinder("content://sms/inbox").setColumns(columns)
                .setWhere("type=1 AND read=0").setCountName(VariableNames.SMS_UNREAD_COUNT);
    }
    
    private static void fillUnreadMms(VariableBinderManager m) {
        String columns[] = {
            "_id"
        };
        /*AND seen=0*/
        m.addContentProviderBinder("content://mms/inbox").setColumns(columns)
                .setWhere("msg_box=1 AND read=0").setCountName(VariableNames.MMS_UNREAD_COUNT);
    }
}
