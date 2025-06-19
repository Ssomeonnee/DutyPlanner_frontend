package com.example.myapplication.ui.fragment

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.example.myapplication.databinding.DeleteNotificationBinding

class DeleteDialogFragment : DialogFragment() {
    private var _binding: DeleteNotificationBinding? = null
    private val binding get() = _binding!!
    private var onDeleteConfirmed: (() -> Unit)? = null

    companion object {
        fun newInstance(
            title: String,
            message: String,
            deleteName: String,
            onDeleteConfirmed: () -> Unit
        ): DeleteDialogFragment {
            return DeleteDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("title", title)
                    putString("message", message)
                    putString("deleteName", deleteName)
                }
                this.onDeleteConfirmed = onDeleteConfirmed
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DeleteNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            binding.titleTextView.text = it.getString("title", "")
            binding.descriptionTextView.text = it.getString("message", "")
            binding.deleteButton.text = it.getString("deleteName", "")
        }

        binding.cancelButton.setOnClickListener { dismiss() }
        binding.deleteButton.setOnClickListener {
            onDeleteConfirmed?.invoke()
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}