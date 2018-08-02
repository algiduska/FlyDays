package com.example.android.flydays;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.edmodo.rangebar.RangeBar;

import java.text.DecimalFormat;


public class FilterDialog extends AppCompatDialogFragment {

    private TextView outDepMinView;
    private TextView outDepMaxView;
    private TextView outArrMinView;
    private TextView outArrMaxView;
    private TextView retDepMinView;
    private TextView retDepMaxView;
    private TextView retArrMinView;
    private TextView retArrMaxView;
    private FilterDialogListener listener;
    private RangeBar outDepBar;
    private RangeBar outArrBar;
    private RangeBar retDepBar;
    private RangeBar retArrBar;
    private DecimalFormat deciFormat= new DecimalFormat("00");
    private String outDepMin;
    private String outDepMax;
    private String outArrMin;
    private String outArrMax;
    private String retDepMin;
    private String retDepMax;
    private String retArrMin;
    private String retArrMax;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.filter_activity, null);

        builder.setView(view)
                .setTitle("Filter")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        outDepMin = outDepMinView.getText().toString();
                        outDepMax = outDepMaxView.getText().toString();
                        outArrMin = outArrMinView.getText().toString();
                        outArrMax = outArrMaxView.getText().toString();
                        retDepMin = retDepMinView.getText().toString();
                        retDepMax = retDepMaxView.getText().toString();
                        retArrMin = retArrMinView.getText().toString();
                        retArrMax = retArrMaxView.getText().toString();

                        listener.sendData(outDepMin, outDepMax, outArrMin, outArrMax, retDepMin,
                                retDepMax, retArrMin, retArrMax);
                    }
                });

        outDepMaxView = view.findViewById(R.id.out_dep_max);
        outDepMinView = view.findViewById(R.id.out_dep_min);
        outArrMaxView = view.findViewById(R.id.out_arr_max);
        outArrMinView = view.findViewById(R.id.out_arr_min);
        retDepMaxView = view.findViewById(R.id.ret_dep_max);
        retDepMinView = view.findViewById(R.id.ret_dep_min);
        retArrMaxView = view.findViewById(R.id.ret_arr_max);
        retArrMinView = view.findViewById(R.id.ret_arr_min);

        //using edmodo library to create a range seekbar https://github.com/edmodo/range-bar or wiki https://github.com/edmodo/range-bar/wiki
        //code inspired by https://stackoverflow.com/questions/37064925/hh-mm-in-range-bar
        outDepBar = (RangeBar) view.findViewById(R.id.outgoing_dep);
        setVariables(outDepBar);
        outArrBar = (RangeBar) view.findViewById(R.id.outgoing_arr);
        setVariables(outArrBar);
        retDepBar = (RangeBar) view.findViewById(R.id.return_dep);
        setVariables(retDepBar);
        retArrBar = (RangeBar) view.findViewById(R.id.return_arr);
        setVariables(retArrBar);



        outDepBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onIndexChangeListener(RangeBar rangeBar, int leftThumbIndex, int rightThumbIndex) {
                //range is 49 for 48 possible states, 2 for each hour in a day (+1 for index)
                int minHour = leftThumbIndex / 2;
                int minMinute = 30 * (leftThumbIndex % 2);
                int maxHour = rightThumbIndex / 2;
                int maxMinute = 30 * (rightThumbIndex % 2);
                outDepMinView.setText(deciFormat.format(minHour) + ":" + deciFormat.format(minMinute));
                outDepMaxView.setText(deciFormat.format(maxHour) + ":" + deciFormat.format(maxMinute));
            }
        });

        outArrBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onIndexChangeListener(RangeBar rangeBar, int leftThumbIndex, int rightThumbIndex) {
                //range is 49 for 48 possible states, 2 for each hour in a day (+1 for index)
                int minHour = leftThumbIndex / 2;
                int minMinute = 30 * (leftThumbIndex % 2);
                int maxHour = rightThumbIndex / 2;
                int maxMinute = 30 * (rightThumbIndex % 2);
                outArrMinView.setText(deciFormat.format(minHour) + ":" + deciFormat.format(minMinute));
                outArrMaxView.setText(deciFormat.format(maxHour) + ":" + deciFormat.format(maxMinute));
            }
        });

        retDepBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onIndexChangeListener(RangeBar rangeBar, int leftThumbIndex, int rightThumbIndex) {
                //range is 49 for 48 possible states, 2 for each hour in a day (+1 for index)
                int minHour = leftThumbIndex / 2;
                int minMinute = 30 * (leftThumbIndex % 2);
                int maxHour = rightThumbIndex / 2;
                int maxMinute = 30 * (rightThumbIndex % 2);
                retDepMinView.setText(deciFormat.format(minHour) + ":" + deciFormat.format(minMinute));
                retDepMaxView.setText(deciFormat.format(maxHour) + ":" + deciFormat.format(maxMinute));
            }
        });

        retArrBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onIndexChangeListener(RangeBar rangeBar, int leftThumbIndex, int rightThumbIndex) {
                //range is 49 for 48 possible states, 2 for each hour in a day (+1 for index)
                int minHour = leftThumbIndex / 2;
                int minMinute = 30 * (leftThumbIndex % 2);
                int maxHour = rightThumbIndex / 2;
                int maxMinute = 30 * (rightThumbIndex % 2);
                retArrMinView.setText(deciFormat.format(minHour) + ":" + deciFormat.format(minMinute));
                retArrMaxView.setText(deciFormat.format(maxHour) + ":" + deciFormat.format(maxMinute));
            }
        });


        return builder.create();
    }

    //use CTRL+O to open possible methods to override
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //for automatically surround a code by try/catch or if/else etc, highlight the part and CTRL+ALT+T
        try {
            listener = (FilterDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement FilterDialogListener");
        }
    }

    /**
     * sends the data from filterActivity by implementing it in Main activity thus making
     * the data is accessible
     */
    public interface FilterDialogListener{
        void sendData(String outDepMin, String outDepMax, String outArrMin, String outArrMax,
                      String retDepMin, String retDepMax, String retArrMin, String retArrMax);
    }

    /**
     * sets variables for each range bar
     * @param rangebar
     */
    public void setVariables(RangeBar rangebar){
        rangebar.setTickCount(48 + 1); //need to start with +1 as a regular index value
        rangebar.setTickHeight(0);
        rangebar.setThumbRadius(8);
        rangebar.setConnectingLineWeight(3);
        //requires android colour - https://convertingcolors.com/hex-color-99B898.html
        rangebar.setBarColor(0xFF99B898);
        rangebar.setConnectingLineColor(0xFF99B898);
        rangebar.setThumbColorNormal(0xFF99B898);
        rangebar.setThumbColorPressed(0xFF99B898);
    }
}
