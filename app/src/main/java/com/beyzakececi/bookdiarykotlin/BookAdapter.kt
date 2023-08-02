package com.beyzakececi.bookdiarykotlin

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.beyzakececi.bookdiarykotlin.databinding.RecyclerRowBinding

class BookAdapter(val bookList: ArrayList<Book>) : RecyclerView.Adapter<BookAdapter.BookHolder>(){

    class BookHolder( val binding : RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookHolder {

        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return BookHolder(binding)
    }

    override fun onBindViewHolder(holder: BookHolder, position: Int) {
        holder.binding.recyclerViewTextView.text = bookList[position].name
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context,BookActivity::class.java)
            intent.putExtra("info","old")
            intent.putExtra("bookId",bookList[position].id)
            holder.itemView.context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return bookList.size
    }
}