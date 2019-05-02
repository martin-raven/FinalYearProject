package in.co.iodev.secondattendance;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hardik on 9/1/17.
 */
public class CustomAdapter  extends BaseAdapter {

    private Context context;
    private List<Model> ModelList;

    public CustomAdapter(Context context, List<Model> ModelList) {

        this.context = context;
        this.ModelList=ModelList;
    }

    @Override
    public int getViewTypeCount() {
        if(getCount() > 0){
            return getCount();
        }else{
            return 1;
        }
    }
    @Override
    public int getItemViewType(int position) {

        return position;
    }

    @Override
    public int getCount() {
        return ModelList.size();
    }

    @Override
    public Object getItem(int position) {
        return ModelList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder(); LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item, null, true);


            holder.tvName = convertView.findViewById(R.id.Name);
            holder.btn_Delete = convertView.findViewById(R.id.DeleteButton);

            convertView.setTag(holder);
        }else {
            // the getTag returns the viewHolder object set as a tag to the view
            holder = (ViewHolder)convertView.getTag();
        }

        holder.tvName.setText(ModelList.get(position).getName());
        holder.btn_Delete.setTag(position);
        holder.btn_Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Name clicked: ",ModelList.get((Integer) v.getTag()).getName());
                CameraActivity cameraActivity=(CameraActivity)context;
                cameraActivity.removeFromlist((Integer) v.getTag());

            }
        });

        return convertView;
    }

    private class ViewHolder {

        protected Button btn_Delete;
        private TextView tvName;

    }

}