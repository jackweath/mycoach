package me.jackweath.mycoach;

import android.content.Context;
import android.graphics.Color;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by jackweatherilt on 01/04/16.
 */
public class GraphFragment extends Fragment {

    private LineChartView chart;
    private LineChartData data;
    private List<Line> lines;
    private String xTitle, yTitle;
    private Axis axisX, axisY;


    public GraphFragment() {
        // Blank, required
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chart, container, false);

        chart = (LineChartView) view.findViewById(R.id.chart);
        chart.setOnValueTouchListener(new ValueTouchListener());

        chart.setZoomType(ZoomType.HORIZONTAL);

        data = new LineChartData();
        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);


        data.setLines(lines);
        chart.setLineChartData(data);
        return view;
    }

    public static GraphFragment newInstance(String xTitle, String yTitle,
                                            List<PointValue> dataPoints) {
        GraphFragment gFrag = new GraphFragment();

        Axis axisX = new Axis();
        Axis axisY = new Axis();

        // No  Y titles in screen mockups
        axisX.setName(xTitle).setTextColor(Color.WHITE);
        //axisY.setName(yTitle).setTextColor(Color.WHITE);

        gFrag.axisX = axisX;
        gFrag.axisY = axisY;

        gFrag.lines = new ArrayList<>();
        gFrag.addLine(dataPoints);

        return gFrag;
    }

    private void addLine(List<PointValue> points) {
        Line line = new Line(points).setColor(Color.WHITE)
                .setHasPoints(true)
                .setFilled(true);
        lines.add(line);
    }

    private class ValueTouchListener implements LineChartOnValueSelectListener {

        @Override
        public void onValueSelected(int lineIndex, int pointIndex, PointValue value) {
            Toast.makeText(getActivity(), "Selected: " + value, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onValueDeselected() {
            // TODO Auto-generated method stub

        }

    }

}
