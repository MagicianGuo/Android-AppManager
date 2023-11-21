package com.gjc.appmanager.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.gjc.appmanager.R
import com.gjc.appmanager.bean.BeanApp

class PackageAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val mList = mutableListOf<BeanApp>()
    private var mOperationListener: IOperationListener? = null

    fun updateList(list: List<BeanApp>) {
        mList.clear()
        mList.addAll(list)
        notifyDataSetChanged()
    }

    fun setOperationListener(listener: IOperationListener) {
        mOperationListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_app_item, parent, false)
        return AppHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is AppHolder) {
            val beanApp = mList[position]
            holder.tvLabel.text = beanApp.label
            holder.tvPackageName.text = beanApp.packageName
            holder.ivIcon.setImageDrawable(beanApp.icon)
            holder.btnOpen.setOnClickListener {
                mOperationListener?.onOpen(beanApp)
            }
            holder.btnDetail.setOnClickListener {
                mOperationListener?.onDetail(beanApp)
            }
            holder.btnUninstall.setOnClickListener {
                mOperationListener?.onUninstall(beanApp)
            }
            holder.tvCompileSdk.let { cs ->
                cs.isVisible = beanApp.compileSdk != -1
                cs.text = "CompileSdk: ${beanApp.compileSdk}"
            }
            holder.tvTargetSdk.text = "TargetSdk: ${beanApp.targetSdk}"
            holder.tvMinSdk.text = "MinSdk: ${beanApp.minSdk}"
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    private class AppHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvLabel: TextView = itemView.findViewById(R.id.tv_label)
        val tvPackageName: TextView = itemView.findViewById(R.id.tv_package_name)
        val ivIcon: ImageView = itemView.findViewById(R.id.iv_icon)
        val btnOpen: TextView = itemView.findViewById(R.id.btn_open)
        val btnDetail: TextView = itemView.findViewById(R.id.btn_detail)
        val btnUninstall: TextView = itemView.findViewById(R.id.btn_uninstall)
        val tvCompileSdk: TextView = itemView.findViewById(R.id.tv_compile_sdk)
        val tvTargetSdk: TextView = itemView.findViewById(R.id.tv_target_sdk)
        val tvMinSdk: TextView = itemView.findViewById(R.id.tv_min_sdk)
    }

    interface IOperationListener {
        fun onOpen(beanApp: BeanApp)
        fun onDetail(beanApp: BeanApp)
        fun onUninstall(beanApp: BeanApp)
    }
}