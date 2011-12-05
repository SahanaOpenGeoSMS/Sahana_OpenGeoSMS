package com.sahana.geosmser;

import com.sahana.geosmser.R;
import com.sahana.geosmser.database.WhiteListDBAdapter;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class GeoSMSContactEditor extends Activity {

    private TextView titleText, interpretWhitelistText;

    private Button editContactButton, backHomeButton;

    private RelativeLayout backgroundLayout;

    private WhiteListDBAdapter db;

    private ListView whiteListView;

    private Context mContext;

    private SimpleCursorAdapter mSimpleCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        backgroundLayout = new RelativeLayout(this);
        setContentView(backgroundLayout);
        init();
        initEvent();
    }

    public void init() {

        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int mWidth = dm.widthPixels;

        titleText = new TextView(this);
        interpretWhitelistText = new TextView(this);

        whiteListView = new ListView(this);

        editContactButton = new Button(this);
        backHomeButton = new Button(this);

        titleText.setId(100);
        titleText.setPadding(0, 0, 0, 10);
        interpretWhitelistText.setId(101);
        interpretWhitelistText.setTextColor(Color.GREEN);
        interpretWhitelistText.setTypeface(Typeface.DEFAULT, 1);
        whiteListView.setId(103);
        whiteListView.setPadding(0, 0, 0, 10);
        editContactButton.setId(104);
        backHomeButton.setId(105);

        RelativeLayout.LayoutParams titleTextParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams interpretWhitelistTextParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams whiteListParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams editContactButtonParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams backHomeButtonParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        titleTextParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        interpretWhitelistTextParams.addRule(RelativeLayout.BELOW,
                titleText.getId());
        interpretWhitelistTextParams.addRule(RelativeLayout.ALIGN_LEFT,
                titleText.getId());
        whiteListParams.addRule(RelativeLayout.BELOW,
                interpretWhitelistText.getId());
        whiteListParams.addRule(RelativeLayout.ALIGN_LEFT,
                interpretWhitelistText.getId());
        whiteListParams
                .addRule(RelativeLayout.ABOVE, editContactButton.getId());
        editContactButtonParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        editContactButtonParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        backHomeButtonParams.addRule(RelativeLayout.RIGHT_OF,
                editContactButton.getId());
        backHomeButtonParams.addRule(RelativeLayout.ALIGN_TOP,
                editContactButton.getId());

        titleText.setText(R.string.contact_editor_readme);
        interpretWhitelistText.setText(R.string.contact_editor_whitelist_title);
        editContactButton.setText(R.string.contact_editor_edit_button);
        backHomeButton.setText(R.string.contact_editor_home_button);

        titleText.setLayoutParams(titleTextParams);
        interpretWhitelistText.setLayoutParams(interpretWhitelistTextParams);
        whiteListView.setLayoutParams(whiteListParams);
        editContactButton.setLayoutParams(editContactButtonParams);
        backHomeButton.setLayoutParams(backHomeButtonParams);

        editContactButton.setWidth(mWidth / 2);

        editContactButton.setOnClickListener(edit);
        backHomeButton.setOnClickListener(home);
        whiteListView.setOnCreateContextMenuListener(configure);

        backgroundLayout.addView(titleText);
        backgroundLayout.addView(interpretWhitelistText);
        backgroundLayout.addView(whiteListView);
        backgroundLayout.addView(editContactButton);
        backgroundLayout.addView(backHomeButton);

    }

    public void initEvent() {
        mContext = this;
        db = new WhiteListDBAdapter(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        db.open();
        Cursor c = db.getAllTitles();
        if (c.moveToFirst()) {

            mSimpleCursorAdapter = new SimpleCursorAdapter(this,
                    R.layout.whitelist_row_layout, c, new String[] {
                            WhiteListDBAdapter.KEY_NAME, WhiteListDBAdapter.KEY_NUMBER },
                    new int[] { R.id.showText1, R.id.showText2 });
            whiteListView.setAdapter(mSimpleCursorAdapter);

            // do {
            // listText.append(Html.fromHtml("<b>" + c.getString(1)+ "</b>"
            // +"  "+ "<small>" + c.getString(2)+ "</small>" + "<br />" ));
            // } while(c.moveToNext());
        }

        db.close();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    public static final int CONTEXT_MENU_DELETE_ID = 200;

    public ListView.OnCreateContextMenuListener configure = new ListView.OnCreateContextMenuListener() {

        @Override
        public void onCreateContextMenu(ContextMenu menu, View view,
                ContextMenuInfo menuInfo) {
            menu.add(0, CONTEXT_MENU_DELETE_ID, 1,
                    R.string.context_menu_geosms_inbox_delete);

        }

    };

    private static final int WHITELIST_DELETE_TOKEN = 300;

    private static final String WHITELIST_KEY_WORD = "UserName";

    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        case CONTEXT_MENU_DELETE_ID:
            db.open();
            AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
                    .getMenuInfo();
            View view = menuInfo.targetView;
            LinearLayout mLayout = ((LinearLayout) view);
            String userName = ((TextView) mLayout.getChildAt(0)).getText()
                    .toString();

            Bundle bundle = new Bundle();
            bundle.putString(WHITELIST_KEY_WORD, userName);
            MyHandler mHandler = new MyHandler();
            Message msg = new Message();
            msg.setData(bundle);
            msg.what = WHITELIST_DELETE_TOKEN;
            mHandler.sendMessage(msg);
            db.close();
        }

        return true;
    }

    public Button.OnClickListener edit = new Button.OnClickListener() {

        @Override
        public void onClick(View arg0) {
            startActivity(new Intent(GeoSMSContactEditor.this,
                    ContactListAct.class));

        }

    };

    public Button.OnClickListener home = new Button.OnClickListener() {

        @Override
        public void onClick(View arg0) {
            startActivity(new Intent(GeoSMSContactEditor.this, WhereToMeet.class));
            finish();
        }

    };

    public class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case WHITELIST_DELETE_TOKEN:
                if (db.deleteTitle(isThere(msg.getData().getString(
                        WHITELIST_KEY_WORD)))) {
                    Toast.makeText(
                            GeoSMSContactEditor.this,
                            msg.getData().getString(WHITELIST_KEY_WORD)
                                    + " is removed from white list..", Toast.LENGTH_SHORT)
                            .show();
                }
                mSimpleCursorAdapter.changeCursor(db.getAllTitles());
                break;
            }
        }

    }

    /**
     * Mapping user name to row_id
     * 
     * @param name
     * @return RowID
     */
    public int isThere(String name) {
        db.open();
        Cursor c = db.getAllTitles();
        if (c.moveToFirst()) {
            do {
                if (c.getString(1).equalsIgnoreCase(name)) {
                    int id = Integer.parseInt(c.getString(0));
                    // db.close();
                    return id;
                }
            } while (c.moveToNext());
        }
        db.close();
        return -1;
    }
}
