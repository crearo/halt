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
import io.reactivex.subjects.PublishSubject
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

@AndroidEntryPoint
class UnlockStatFragment : Fragment() {

    @Inject
    lateinit var repository: UnlockStatRepository

    private val compositeDisposable = CompositeDisposable()
    private var _binding: FragmentUnlockStatBinding? = null
    private val binding get() = _binding!!
    private var startLocalDateTimeInUtc =
        LocalDate.now(ZoneId.systemDefault()).atStartOfDay(ZoneId.systemDefault())
    private val endLocalDateTime get() = startLocalDateTimeInUtc.plusDays(1)
    private val localDateSubject = PublishSubject.create<ZonedDateTime>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUnlockStatBinding.inflate(layoutInflater, container, false)

        binding.btnMinusDay.setOnClickListener {
            startLocalDateTimeInUtc = startLocalDateTimeInUtc.minusDays(1)
            localDateSubject.onNext(startLocalDateTimeInUtc)
        }
        binding.btnPlusDay.setOnClickListener {
            startLocalDateTimeInUtc = startLocalDateTimeInUtc.plusDays(1)
            localDateSubject.onNext(startLocalDateTimeInUtc)
        }

        compositeDisposable.add(localDateSubject.subscribe {
            binding.tvDay.text = startLocalDateTimeInUtc.toString()
            setUsageStatsTextViews()
        })

        localDateSubject.onNext(startLocalDateTimeInUtc)

        return binding.root
    }

    private fun setUsageStatsTextViews() {
        compositeDisposable.add(repository
            .getTotalTimeUsed(
                startLocalDateTimeInUtc.toInstant(),
                endLocalDateTime.toInstant()
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
                .getFirstUnlock(startLocalDateTimeInUtc.toLocalDate()/*fixme: now this is wrong. I should change the underlying call to be sure exactly what date I am looking for*/)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess {
                    binding.tvFirstUnlock.text =
                        it.unlockTime.atZone(ZoneId.systemDefault()).toString()
                }
                .doOnError { binding.tvFirstUnlock.text = "Failed to get first time used" }
                .subscribe { t1, t2 -> }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.clear()
        _binding = null
    }

}