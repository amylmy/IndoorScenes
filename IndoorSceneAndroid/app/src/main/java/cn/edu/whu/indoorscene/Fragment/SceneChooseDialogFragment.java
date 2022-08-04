package cn.edu.whu.indoorscene.Fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import java.util.ArrayList;

/**
 * Created by v-mengyl on 2015/1/25.
 *
 */
public class SceneChooseDialogFragment extends DialogFragment {

    ArrayList<String> sceneList = new ArrayList<>();
    String dialogTitle;
    String sceneType;
    public static String currentScene;

    public static SceneChooseDialogFragment newInstance(ArrayList<String> sceneList, String dialogTitle, String sceneType) {
        SceneChooseDialogFragment fragment = new SceneChooseDialogFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("sceneList", sceneList);
        args.putString("title", dialogTitle);
        args.putString("sceneType", sceneType);
//        args.putString("title", title);
//        args.putString("traceID", traceID);
        fragment.setArguments(args);
        return fragment;
    }

    public interface sceneChooseListener
    {
        void onSceneChooseListener(String currentScene, String sceneType);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        sceneList = getArguments().getStringArrayList("sceneList");
        dialogTitle = getArguments().getString("title");
        sceneType = getArguments().getString("sceneType");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        String[] corridors = {"other", "lmars1f", "office2007", "corridor", "elevator", "meeting_room"};
//        for (int i = 0; i<corridors.length; i++) {
//            if (!sceneList.contains(corridors[i])){
//                sceneList.add(corridors[i]);
//            }
//        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, sceneList);

        builder.setTitle(dialogTitle)
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                currentScene = sceneList.get(which);
                sceneChooseListener listener = (sceneChooseListener)getActivity();
                listener.onSceneChooseListener(currentScene, sceneType);
                //Toast.makeText(getContext(),"You hava chosen " + currentScene, Toast.LENGTH_SHORT).show();
            }
        });

        return builder.create();
    }
}
