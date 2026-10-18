import React, { useState, useMemo } from 'react';
import { motion } from 'motion/react';
import { Calendar, CheckCircle2, Flame, TrendingUp, AlertTriangle, Filter } from 'lucide-react';
import { Medication, DoseLog, DoseStatus } from '../types';
import { medAccent } from '../theme/tokens';
import { MedIcon } from '../components/MedIcon';
import { soundManager } from '../audio/soundManager';

interface ProgressScreenProps {
  medications: Medication[];
  doseLogs: DoseLog[];
  onSelectDate: (date: Date) => void;
  onNavigateHome: () => void;
}

export const ProgressScreen: React.FC<ProgressScreenProps> = ({
  medications,
  doseLogs,
  onSelectDate,
  onNavigateHome,
}) => {
  const [filterStatus, setFilterStatus] = useState<'ALL' | 'TAKEN' | 'SKIPPED'>('ALL');

  // Adherence calculation for past 30 days
  const thirtyDayStats = useMemo(() => {
    const now = Date.now();
    const thirtyDaysAgo = now - 30 * 24 * 3600 * 1000;
    const relevant = doseLogs.filter((l) => l.scheduledTime >= thirtyDaysAgo && l.scheduledTime <= now);

    const total = relevant.length;
    const taken = relevant.filter((l) => l.status === 'TAKEN').length;
    const skipped = relevant.filter((l) => l.status === 'SKIPPED').length;
    const rate = total > 0 ? Math.round((taken / total) * 100) : 0;

    return { total, taken, skipped, rate };
  }, [doseLogs]);

  // Daily calendar heatmap (last 28 days = 4 full weeks)
  const heatmapDays = useMemo(() => {
    const days = [];
    const now = new Date();
    now.setHours(0, 0, 0, 0);

    for (let i = 27; i >= 0; i--) {
      const d = new Date(now);
      d.setDate(now.getDate() - i);
      const start = new Date(d).setHours(0, 0, 0, 0);
      const end = new Date(d).setHours(23, 59, 59, 999);

      const logs = doseLogs.filter((l) => l.scheduledTime >= start && l.scheduledTime <= end);
      const total = logs.length;
      const taken = logs.filter((l) => l.status === 'TAKEN').length;
      const ratio = total > 0 ? taken / total : 0;

      days.push({
        date: d,
        total,
        taken,
        ratio,
        isToday: i === 0,
      });
    }
    return days;
  }, [doseLogs]);

  // Streak calculation
  const streak = useMemo(() => {
    let currentStreak = 0;
    const now = new Date();
    now.setHours(0, 0, 0, 0);

    for (let i = 0; i < 30; i++) {
      const d = new Date(now);
      d.setDate(now.getDate() - i);
      const start = new Date(d).setHours(0, 0, 0, 0);
      const end = new Date(d).setHours(23, 59, 59, 999);

      const logs = doseLogs.filter((l) => l.scheduledTime >= start && l.scheduledTime <= end);
      if (logs.length === 0) continue;

      const allTaken = logs.every((l) => l.status === 'TAKEN');
      if (allTaken) {
        currentStreak++;
      } else {
        if (i === 0) {
          // Today might still be in progress
          continue;
        }
        break;
      }
    }
    return currentStreak;
  }, [doseLogs]);

  // Medication breakdown
  const medBreakdown = useMemo(() => {
    return medications.map((med) => {
      const medLogs = doseLogs.filter((l) => l.medicationId === med.id);
      const total = medLogs.length;
      const taken = medLogs.filter((l) => l.status === 'TAKEN').length;
      const rate = total > 0 ? Math.round((taken / total) * 100) : 100;
      return { med, total, taken, rate };
    });
  }, [medications, doseLogs]);

  // Filtered recent logs
  const recentLogs = useMemo(() => {
    return doseLogs
      .filter((l) => {
        if (filterStatus === 'TAKEN') return l.status === 'TAKEN';
        if (filterStatus === 'SKIPPED') return l.status === 'SKIPPED';
        return true;
      })
      .slice(-25)
      .reverse();
  }, [doseLogs, filterStatus]);

  return (
    <div className="w-full max-w-xl mx-auto px-4 pb-28 pt-4">
      {/* Header */}
      <div className="mb-6">
        <span className="text-[11px] font-semibold tracking-[0.2em] text-[#8C9287] dark:text-[#73796E] uppercase">
          INSIGHTS & ADHERENCE
        </span>
        <h1 className="text-2xl font-bold text-[#141613] dark:text-[#EDEDEA] mt-1 font-sans">
          Progress
        </h1>
      </div>

      {/* Hero Stats Card */}
      <div className="grid grid-cols-2 gap-3 mb-6">
        <div className="p-4 rounded-2xl bg-[#FFFFFF] dark:bg-[#1C1F1A] border border-[#E2E1D9] dark:border-[#292E26] shadow-xs">
          <div className="flex items-center gap-2 text-[#516351] dark:text-[#7B947B]">
            <TrendingUp size={16} />
            <span className="text-[11px] font-bold tracking-wider uppercase">30-Day Rate</span>
          </div>
          <div className="mt-2 flex items-baseline gap-1">
            <span className="text-3xl font-bold text-[#141613] dark:text-[#EDEDEA]">
              {thirtyDayStats.rate}%
            </span>
            <span className="text-xs text-[#8C9287] dark:text-[#73796E]">taken</span>
          </div>
          <p className="text-xs text-[#8C9287] dark:text-[#73796E] mt-1">
            {thirtyDayStats.taken} of {thirtyDayStats.total} doses
          </p>
        </div>

        <div className="p-4 rounded-2xl bg-[#FFFFFF] dark:bg-[#1C1F1A] border border-[#E2E1D9] dark:border-[#292E26] shadow-xs">
          <div className="flex items-center gap-2 text-[#9A5637] dark:text-[#D97A52]">
            <Flame size={16} />
            <span className="text-[11px] font-bold tracking-wider uppercase">Daily Streak</span>
          </div>
          <div className="mt-2 flex items-baseline gap-1">
            <span className="text-3xl font-bold text-[#141613] dark:text-[#EDEDEA]">
              {streak}
            </span>
            <span className="text-xs text-[#8C9287] dark:text-[#73796E]">days</span>
          </div>
          <p className="text-xs text-[#8C9287] dark:text-[#73796E] mt-1">
            Consistent routine
          </p>
        </div>
      </div>

      {/* 28-Day Heatmap Grid */}
      <div className="mb-6 p-4 rounded-2xl bg-[#FFFFFF] dark:bg-[#1C1F1A] border border-[#E2E1D9] dark:border-[#292E26] shadow-xs">
        <div className="flex items-center justify-between mb-3">
          <div className="flex items-center gap-2">
            <Calendar size={15} className="text-[#8C9287] dark:text-[#73796E]" />
            <h3 className="text-xs font-bold tracking-wider uppercase text-[#141613] dark:text-[#EDEDEA]">
              Activity Grid (4 Weeks)
            </h3>
          </div>
          <span className="text-[11px] text-[#8C9287] dark:text-[#73796E]">
            Tap day to view
          </span>
        </div>

        <div className="grid grid-cols-7 gap-2">
          {['M', 'T', 'W', 'T', 'F', 'S', 'S'].map((d, i) => (
            <div key={i} className="text-center text-[10px] font-bold text-[#8C9287] dark:text-[#73796E] mb-1">
              {d}
            </div>
          ))}
          {heatmapDays.map((item, idx) => {
            let bgClass = 'bg-[#ECEBE4] dark:bg-[#242822]';
            if (item.total > 0) {
              if (item.ratio === 1) bgClass = 'bg-[#516351] dark:bg-[#7B947B] text-white';
              else if (item.ratio >= 0.5) bgClass = 'bg-[#516351]/50 dark:bg-[#7B947B]/50 text-white';
              else bgClass = 'bg-[#9A5637]/40 dark:bg-[#D97A52]/40 text-[#9A5637] dark:text-[#D97A52]';
            }

            return (
              <button
                key={idx}
                onClick={() => {
                  soundManager.playSoftTap();
                  onSelectDate(item.date);
                  onNavigateHome();
                }}
                title={`${item.date.toLocaleDateString()}: ${item.taken}/${item.total} taken`}
                className={`aspect-square rounded-xl flex flex-col items-center justify-center text-xs font-semibold transition-transform hover:scale-110 cursor-pointer ${bgClass} ${
                  item.isToday ? 'ring-2 ring-[#516351] dark:ring-[#7B947B]' : ''
                }`}
              >
                <span>{item.date.getDate()}</span>
              </button>
            );
          })}
        </div>

        <div className="mt-4 flex items-center justify-end gap-3 text-[10px] text-[#8C9287] dark:text-[#73796E]">
          <span className="flex items-center gap-1">
            <span className="w-2.5 h-2.5 rounded bg-[#ECEBE4] dark:bg-[#242822]" />
            <span>None</span>
          </span>
          <span className="flex items-center gap-1">
            <span className="w-2.5 h-2.5 rounded bg-[#516351]/50 dark:bg-[#7B947B]/50" />
            <span>Partial</span>
          </span>
          <span className="flex items-center gap-1">
            <span className="w-2.5 h-2.5 rounded bg-[#516351] dark:bg-[#7B947B]" />
            <span>Complete</span>
          </span>
        </div>
      </div>

      {/* Medication Breakdown */}
      <div className="mb-6 p-4 rounded-2xl bg-[#FFFFFF] dark:bg-[#1C1F1A] border border-[#E2E1D9] dark:border-[#292E26] shadow-xs">
        <h3 className="text-xs font-bold tracking-wider uppercase text-[#141613] dark:text-[#EDEDEA] mb-3">
          Medication Adherence
        </h3>

        <div className="space-y-3">
          {medBreakdown.map(({ med, taken, total, rate }) => {
            const accent = medAccent(med.colorTag);
            return (
              <div key={med.id}>
                <div className="flex items-center justify-between text-xs mb-1">
                  <div className="flex items-center gap-2">
                    <span
                      style={{ backgroundColor: accent }}
                      className="w-2 h-2 rounded-full shrink-0"
                    />
                    <span className="font-semibold text-[#141613] dark:text-[#EDEDEA]">
                      {med.name}
                    </span>
                  </div>
                  <span className="font-bold text-[#5E645A] dark:text-[#A3A89F]">
                    {rate}% ({taken}/{total})
                  </span>
                </div>

                <div className="w-full h-2 rounded-full bg-[#ECEBE4] dark:bg-[#242822] overflow-hidden">
                  <div
                    style={{
                      width: `${rate}%`,
                      backgroundColor: accent,
                    }}
                    className="h-full rounded-full transition-all duration-500"
                  />
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Dose Log History */}
      <div className="p-4 rounded-2xl bg-[#FFFFFF] dark:bg-[#1C1F1A] border border-[#E2E1D9] dark:border-[#292E26] shadow-xs">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-xs font-bold tracking-wider uppercase text-[#141613] dark:text-[#EDEDEA]">
            Recent Logs
          </h3>

          <div className="flex items-center gap-1">
            {(['ALL', 'TAKEN', 'SKIPPED'] as const).map((status) => (
              <button
                key={status}
                onClick={() => {
                  soundManager.playSoftTap();
                  setFilterStatus(status);
                }}
                className={`px-2 py-0.5 text-[10px] font-bold tracking-wider rounded-md transition-colors cursor-pointer ${
                  filterStatus === status
                    ? 'bg-[#516351] dark:bg-[#7B947B] text-white dark:text-[#0E130E]'
                    : 'text-[#8C9287] dark:text-[#73796E] hover:bg-[#ECEBE4] dark:hover:bg-[#242822]'
                }`}
              >
                {status}
              </button>
            ))}
          </div>
        </div>

        <div className="space-y-2 max-h-72 overflow-y-auto pr-1">
          {recentLogs.length === 0 ? (
            <p className="text-xs text-[#8C9287] dark:text-[#73796E] py-4 text-center">
              No logs found for this filter.
            </p>
          ) : (
            recentLogs.map((log) => {
              const med = medications.find((m) => m.id === log.medicationId);
              const isTaken = log.status === 'TAKEN';
              const isSkipped = log.status === 'SKIPPED';
              const logDate = new Date(log.scheduledTime);

              return (
                <div
                  key={log.id}
                  className="flex items-center justify-between py-2 border-b border-[#E2E1D9]/60 dark:border-[#292E26]/60 text-xs"
                >
                  <div className="flex items-center gap-2.5">
                    <span
                      className={`w-2 h-2 rounded-full ${
                        isTaken
                          ? 'bg-[#516351] dark:bg-[#7B947B]'
                          : isSkipped
                          ? 'bg-[#9A5637] dark:bg-[#D97A52]'
                          : 'bg-[#8A6B22]'
                      }`}
                    />
                    <div>
                      <span className="font-semibold text-[#141613] dark:text-[#EDEDEA]">
                        {med ? med.name : 'Medication'}
                      </span>
                      <span className="text-[10px] text-[#8C9287] dark:text-[#73796E] block">
                        {logDate.toLocaleDateString(undefined, { month: 'short', day: 'numeric' })} at{' '}
                        {logDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </span>
                    </div>
                  </div>

                  <span
                    className={`text-[11px] font-semibold px-2 py-0.5 rounded ${
                      isTaken
                        ? 'bg-[#E6ECE5] dark:bg-[#222B22] text-[#516351] dark:text-[#7B947B]'
                        : isSkipped
                        ? 'bg-[#F8E8E2] dark:bg-[#2F1B14] text-[#9A5637] dark:text-[#D97A52]'
                        : 'bg-[#ECEBE4] text-[#5E645A]'
                    }`}
                  >
                    {log.status}
                  </span>
                </div>
              );
            })
          )}
        </div>
      </div>
    </div>
  );
};
