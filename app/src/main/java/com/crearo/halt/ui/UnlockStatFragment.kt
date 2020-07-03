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
import io.reactivex.subjects.BehaviorSubject
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
    private val zonedDateTimeSubject = BehaviorSubject.create<ZonedDateTime>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUnlockStatBinding.inflate(layoutInflater, container, false)

        binding.btnMinusDay.setOnClickListener {
            val startZonedDateTime = zonedDateTimeSubject.value!!
            zonedDateTimeSubject.onNext(startZonedDateTime.minusDays(1))
        }
        binding.btnPlusDay.setOnClickListener {
            val startZonedDateTime = zonedDateTimeSubject.value!!
            zonedDateTimeSubject.onNext(startZonedDateTime.plusDays(1))
        }

        compositeDisposable.add(zonedDateTimeSubject.subscribe {
            binding.tvDay.text = it.toString()
            setUsageStatsTextViews(it)
        })

        zonedDateTimeSubject.onNext(
            LocalDate.now(ZoneId.systemDefault()).atStartOfDay(ZoneId.systemDefault())
        )

        return binding.root
    }

    private fun setUsageStatsTextViews(startZonedDateTime: ZonedDateTime) {
        compositeDisposable.add(repository
            .getTotalTimeUsed(
                startZonedDateTime.toInstant(),
                startZonedDateTime.plusDays(1).toInstant()
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess {
                binding.tvTotalTimeUsed.text =
                    "Total Time Used: ${DateUtils.formatElapsedTime(it.seconds)}"
            }
            .doOnError { binding.tvTotalTimeUsed.text = "Failed to get total time used" }
            .subscribe { t1, t2 -> }
        )
        compositeDisposable.add(
            repository
                .getFirstUnlock(startZonedDateTime.toLocalDate()/*fixme: now this is wrong. I should change the underlying call to be sure exactly what date I am looking for*/)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess {
                    binding.tvFirstUnlock.text =
                        "First Time Used: ${it.unlockTime.atZone(ZoneId.systemDefault())}"
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