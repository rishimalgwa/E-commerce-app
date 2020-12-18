package ViewHolder;



import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommercedemo.R;

import Interface.ItemClickListner;

public class ProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView textProductName, textProductDescription, textProductPrice;
    public ImageView imageView;
    public ItemClickListner listner;

    public ProductViewHolder(View view) {
        super(view);

            imageView = (ImageView) view.findViewById(R.id.product_image);

            textProductName =(TextView) view.findViewById(R.id.product_name);

            textProductDescription = (TextView) view.findViewById(R.id.product_description);

            textProductPrice = (TextView) view.findViewById(R.id.product_price);

    }
public void setItemClickListner(ItemClickListner listner){
        this.listner= listner;
}
    @Override
    public void onClick(View view) {
        listner.onClick(view,getAdapterPosition(),false);
    }
}
