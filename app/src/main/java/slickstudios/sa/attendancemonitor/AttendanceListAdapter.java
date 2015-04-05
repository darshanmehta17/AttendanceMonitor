package slickstudios.sa.attendancemonitor;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Darshan on 24-03-2015.
 */
public class AttendanceListAdapter extends RecyclerView.Adapter<AttendanceListAdapter.ListViewHolder>{

    private List<ListItem> listItems;

    public AttendanceListAdapter(List<ListItem> listItems){
        this.listItems=listItems;
    }


    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView= LayoutInflater.from(parent.getContext()).inflate(R.layout.attendance_list_view,parent,false);
        return (new ListViewHolder(itemView));
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        ListItem listItem=listItems.get(position);
        holder.tvName.setText(listItem.name);
        holder.tvTime.setText(listItem.time);
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    public class ListViewHolder extends RecyclerView.ViewHolder{

        protected TextView tvName,tvTime;

        public ListViewHolder(View view){
            super(view);
            tvName=(TextView)view.findViewById(R.id.tvName);
            tvTime=(TextView)view.findViewById(R.id.tvTime);
        }

    }

}
