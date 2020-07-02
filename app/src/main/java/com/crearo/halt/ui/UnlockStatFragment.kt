package com.crearo.halt.ui

import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.crearo.halt.data.UnlockStatRepository
import com.crearo.halt.databinding.FragmentUnlockStatBinding
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject

@AndroidEntryPoint
class UnlockStatFragment : Fragment() {

    @Inject
    lateinit var repository: UnlockStatRepository

    private val compositeDisposable = CompositeDisposable()
    private var _binding: FragmentUnlockStatBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUnlockStatBinding.inflate(layoutInflater, container, false)
        compositeDisposable.add(repository
            .getTotalTimeUsed(
                LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC),
                Instant.now()
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess {
                binding.tvTotalTimeUsed.text = DateUtils.formatElapsedTime(it.seconds)
            }
            .doOnError { binding.tvTotalTimeUsed.text = "Failed to get total time used" }
            .subscribe { t1, t2 -> }
        )
        compositeDisposable.add(
            repository
                .getFirstUnlock(LocalDate.now())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess { binding.tvFirstUnlock.text = it.unlockTime.toString() }
                .doOnError { binding.tvFirstUnlock.text = "Failed to get first time used" }
                .subscribe { t1, t2 -> }
        )
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.clear()
        _binding = null
    }

}