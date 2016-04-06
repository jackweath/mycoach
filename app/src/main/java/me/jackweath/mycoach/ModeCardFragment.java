package me.jackweath.mycoach;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by jackweatherilt on 31/03/16.
 */
public class ModeCardFragment extends Fragment {
    public String mTitle;
    public String mDesc;
    public String mMode;
    public ModeCardFragment() {

    }

    public static ModeCardFragment newInstance(String title, String desc, String pMode) {
        ModeCardFragment card = new ModeCardFragment();
        card.mTitle = title;
        card.mDesc = desc;
        card.mMode = pMode;

        return card;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mode_card, container, false);

        TextView titleText = (TextView) view.findViewById(R.id.titleText);
        TextView descText = (TextView) view.findViewById(R.id.descText);

        titleText.setText(this.mTitle);
        descText.setText(this.mDesc);

        ImageView header = (ImageView) view.findViewById(R.id.header);


        // TODO: Get the correct sizes (currently they are 0)
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);

        //int width = metrics.widthPixels; // If I have one, and not the other (i.e. set to 0), I think it still works
        //int height = (int) Math.round(9.0/16.0 * width); // What is the height is dp not pix?

        int width = 0;
        int height = 196; // dp specified in xml?

        Log.d("IMAGE", "(height, width) " + height + ", " + width);

        int resource;

        String open = getString(R.string.open_mode);
        String sprint = getString(R.string.sprint_mode);
        String pace = getString(R.string.pace_mode);

        if(this.mMode.equals(open)) {
            resource = R.drawable.open;
        } else if (this.mMode.equals(sprint)) {
            resource = R.drawable.sprint;
        } else if (this.mMode.equals(pace)) {
            resource = R.drawable.pace;
        } else {
            resource = R.drawable.open;
        }

        header.setImageBitmap(
                ImageHelper.resBitmap(
                        getResources(),
                        resource,
                        width, height));

        return view;
    }
}
