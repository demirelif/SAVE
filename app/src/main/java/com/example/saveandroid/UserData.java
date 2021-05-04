package com.example.saveandroid;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;

public class UserData extends Activity {

    private static String TAG = "Data";
    private String[] dataPie = {"Fear", "Sadness", "Anger", "Fatigue"}; // iks
    private int[] values = {10,206,20,10}; // y

    //

    //private int[] heartRates = {69,70,68,68,71};
    private int[] axisData = {60, 62, 64, 66, 68, 70, 72, 74, 76, 78, 80, 82, 84, 86, 88, 90};
    int[] yAxisData = {65, 60, 70, 69, 60, 60, 65, 61, 63, 65, 66, 67};

    PieChart pieChart;
    LineChart lineChart;
    LineChartView lineChartView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_data);
        Log.d(TAG, "oncreate");

        pieChart = (PieChart) findViewById(R.id.idPieChart);
        pieChart.setDescription("Moods and Fatigue");
        addDataSet();
        //lineChart = (LineChart) findViewById(R.id.idLineChart);

        lineChartView = findViewById(R.id.idLineChart);
        drawLine();
    }


    private void addDataSet() {
        Log.d(TAG, "data set");
        ArrayList<PieEntry> yEntry = new ArrayList<>();
        ArrayList<String> entry = new ArrayList<>();

        for ( int i = 0; i < values.length; i++){
            yEntry.add(new PieEntry(values[i],i));
        }

        for ( int i = 0; i < dataPie.length; i++){
            entry.add(dataPie[i]);
        }

        PieDataSet pieDataSet = new PieDataSet(yEntry, "Moods");
        pieDataSet.setSliceSpace(2);
        pieDataSet.setValueTextSize(12);

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.BLUE);
        colors.add(Color.YELLOW);
       // colors.add(Color.MAGENTA);
        colors.add(Color.GRAY);
        colors.add(Color.GREEN);
        colors.add(Color.DKGRAY);

        pieDataSet.setColors(colors);

        Legend legend = pieChart.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setPosition(Legend.LegendPosition.LEFT_OF_CHART);

        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();

    }


    private void drawLine(){
        Log.d(TAG, "line diagram");
        List yAxisValues = new ArrayList();
        List axisValues = new ArrayList();
        Line line = new Line(yAxisValues).setColor(Color.parseColor("#9C27B0"));


        for(int i = 0; i < axisData.length; i++){
            axisValues.add(i, new AxisValue(i).setLabel(String.valueOf(axisData[i])));
        }

        for (int i = 0; i < yAxisData.length; i++){
            yAxisValues.add(new PointValue(i, yAxisData[i]));
        }

       // Line line = new Line(yAxisValues).setColor(Color.parseColor("#9C27B0"));

        List lines = new ArrayList();
        lines.add(line);

        LineChartData data = new LineChartData();
        data.setLines(lines);

        lineChartView.setLineChartData(data);

    }


}