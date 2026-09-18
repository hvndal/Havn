import React, { useState, useMemo } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { Check, Clock, Plus, RotateCcw, X, AlertCircle } from 'lucide-react';
import confetti from 'canvas-confetti';
import { Medication, DoseLog, DoseStatus, User } from '../types';
import { medAccent } from '../theme/tokens';
import { MedIcon } from '../components/MedIcon';
import { HavnButton } from '../components/HavnButton';
import { HavnBreathingMark } from '../components/HavnAmbientField';
import { soundManager } from '../audio/soundManager';

interface HomeScreenProps {
  user: User | null;
  medications: Medication[];
  doseLogs: DoseLog[];
  selectedDate: Date;
  onSelectDate: (date: Date) => void;
  onUpdateDoseStatus: (logId: number, status: DoseStatus) => void;
  onAddMedication: () => void;
  onOpenManageMeds: () => void;
}

export const HomeScreen: React.FC<HomeScreenProps> = ({
  user,
  medications,
  doseLogs,
  selectedDate,
  onSelectDate,
  onUpdateDoseStatus,
  onAddMedication,
  onOpenManageMeds,
}) => {
  const [selectedSlotFilter, setSelectedSlotFilter] = useState<string>('ALL');

  // Format header dates
  const isToday = useMemo(() => {
    const today = new Date();
    return (
      selectedDate.getDate() === today.getDate() &&
      selectedDate.getMonth() === today.getMonth() &&
      selectedDate.getFullYear() === today.getFullYear()
    );
  }, [selectedDate]);

  // Generate 7-day horizontal date strip
  const dateStrip = useMemo(() => {
    const dates = [];
    const base = new Date();
    base.setHours(0, 0, 0, 0);

    for (let i = -3; i <= 3; i++) {
      const d = new Date(base);
      d.setDate(base.getDate() + i);
      dates.push(d);
    }
    return dates;
  }, []);

  // Filter dose logs for selected date
  const dayDoseLogs = useMemo(() => {
    const start = new Date(selectedDate);
    start.setHours(0, 0, 0, 0);
    const end = new Date(selectedDate);
    end.setHours(23, 59, 59, 999);

    return doseLogs
      .filter((l) => l.scheduledTime >= start.getTime() && l.scheduledTime <= end.getTime())
      .map((log) => {
        const med = medications.find((m) => m.id === log.medicationId);
        return { log, med };
      })
      .filter((item): item is { log: DoseLog; med: Medication } => item.med !== undefined)
      .sort((a, b) => a.log.scheduledTime - b.log.scheduledTime);
  }, [selectedDate, doseLogs, medications]);

  // Calculate day completion
  const totalCount = dayDoseLogs.length;
  const takenCount = dayDoseLogs.filter((d) => d.log.status === 'TAKEN').length;
  const progressPercent = totalCount > 0 ? Math.round((takenCount / totalCount) * 100) : 0;

  const handleTakeDose = (logId: number) => {
    soundManager.playSoftChime();
    onUpdateDoseStatus(logId, 'TAKEN');

    // Trigger celebration if this completed the day!
    if (totalCount > 0 && takenCount + 1 >= totalCount) {
      try {
        confetti({
          particleCount: 45,
          spread: 55,
          origin: { y: 0.8 },
          colors: ['#516351', '#9A5637', '#8A6B22', '#7E929A', '#A89878'],
        });
      } catch {}
    }
  };

  const handleUndoDose = (logId: number) => {
    soundManager.playSoftTap();
    onUpdateDoseStatus(logId, 'PENDING');
  };

  const handleSkipDose = (logId: number) => {
    soundManager.playSoftTap();
    onUpdateDoseStatus(logId, 'SKIPPED');
  };

  const formatTimeSlot = (timeStr: string) => {
    if (!timeStr) return '';
    try {
      const [h, m] = timeStr.split(':').map(Number);
      const period = h >= 12 ? 'pm' : 'am';
      const displayH = h % 12 || 12;
      return `${displayH}:${m.toString().padStart(2, '0')} ${period}`;
    } catch {
      return timeStr;
    }
  };

  // Find next upcoming pending dose
  const nextPendingDose = useMemo(() => {
    if (!isToday) return null;
    const now = Date.now();
    return dayDoseLogs.find((d) => d.log.status === 'PENDING' && d.log.scheduledTime >= now - 30 * 60 * 1000);
  }, [isToday, dayDoseLogs]);

  return (
    <div className="w-full max-w-xl mx-auto px-4 pb-28 pt-4">
      {/* Header */}
      <div className="mb-6">
        <div className="flex items-center justify-between">
          <div>
            <span className="text-[11px] font-semibold tracking-[0.2em] text-[#8C9287] dark:text-[#73796E] uppercase">
              {isToday ? "TODAY'S SCHEDULE" : selectedDate.toLocaleDateString(undefined, { weekday: 'long', month: 'short', day: 'numeric' }).toUpperCase()}
            </span>
            <h1 className="text-2xl font-bold text-[#141613] dark:text-[#EDEDEA] mt-1 font-sans">
              {isToday ? 'Today' : selectedDate.toLocaleDateString(undefined, { month: 'short', day: 'numeric' })}
            </h1>
          </div>

          {/* User profile avatar pill */}
          {user && (
            <div
              onClick={onOpenManageMeds}
              className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-[#ECEBE4] dark:bg-[#1C1F1A] border border-[#E2E1D9] dark:border-[#292E26] cursor-pointer hover:opacity-90 transition-opacity"
            >
              <div
                style={{ backgroundColor: user.avatarColor }}
                className="w-5 h-5 rounded-full flex items-center justify-center text-[10px] text-white font-bold"
              >
                {user.name[0]?.toUpperCase()}
              </div>
              <span className="text-xs font-medium text-[#5E645A] dark:text-[#A3A89F]">
                {user.name}
              </span>
            </div>
          )}
        </div>

        {/* 7-Day Horizontal Date Strip */}
        <div className="flex items-center justify-between mt-5 gap-1.5 overflow-x-auto pb-1">
          {dateStrip.map((d) => {
            const isSelected =
              d.getDate() === selectedDate.getDate() &&
              d.getMonth() === selectedDate.getMonth();
            const isCurrToday =
              d.getDate() === new Date().getDate() &&
              d.getMonth() === new Date().getMonth();

            return (
              <button
                key={d.toISOString()}
                onClick={() => {
                  soundManager.playSoftTap();
                  onSelectDate(d);
                }}
                className={`flex-1 min-w-[42px] py-2.5 rounded-xl flex flex-col items-center justify-center transition-all cursor-pointer select-none border ${
                  isSelected
                    ? 'bg-[#516351] dark:bg-[#7B947B] text-white dark:text-[#0E130E] border-[#516351] dark:border-[#7B947B] shadow-xs'
                    : 'bg-[#ECEBE4]/80 dark:bg-[#1C1F1A]/80 text-[#5E645A] dark:text-[#A3A89F] border-transparent hover:bg-[#E2E1D9] dark:hover:bg-[#282E25]'
                }`}
              >
                <span className="text-[10px] uppercase font-semibold tracking-wider opacity-80">
                  {d.toLocaleDateString(undefined, { weekday: 'narrow' })}
                </span>
                <span className="text-sm font-bold mt-0.5">{d.getDate()}</span>
                {isCurrToday && (
                  <span
                    className={`w-1 h-1 rounded-full mt-1 ${
                      isSelected ? 'bg-white dark:bg-black' : 'bg-[#516351] dark:bg-[#7B947B]'
                    }`}
                  />
                )}
              </button>
            );
          })}
        </div>
      </div>

      {/* Daily Progress Card */}
      {totalCount > 0 && (
        <div className="mb-6 p-4 rounded-2xl bg-[#FFFFFF] dark:bg-[#1C1F1A] border border-[#E2E1D9] dark:border-[#292E26] shadow-xs">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="relative w-12 h-12 flex items-center justify-center">
                <svg className="w-12 h-12 transform -rotate-90">
                  <circle
                    cx="24"
                    cy="24"
                    r="20"
                    stroke="currentColor"
                    strokeWidth="3.5"
                    fill="transparent"
                    className="text-[#ECEBE4] dark:text-[#282E25]"
                  />
                  <circle
                    cx="24"
                    cy="24"
                    r="20"
                    stroke="currentColor"
                    strokeWidth="3.5"
                    fill="transparent"
                    strokeDasharray={125.6}
                    strokeDashoffset={125.6 - (125.6 * progressPercent) / 100}
                    strokeLinecap="round"
                    className="text-[#516351] dark:text-[#7B947B] transition-all duration-500"
                  />
                </svg>
                <span className="absolute text-xs font-bold text-[#141613] dark:text-[#EDEDEA]">
                  {progressPercent}%
                </span>
              </div>

              <div>
                <h3 className="text-sm font-semibold text-[#141613] dark:text-[#EDEDEA]">
                  {takenCount} of {totalCount} completed
                </h3>
                <p className="text-xs text-[#8C9287] dark:text-[#73796E] mt-0.5">
                  {progressPercent === 100
                    ? 'All doses recorded for this day'
                    : `${totalCount - takenCount} remaining today`}
                </p>
              </div>
            </div>

            {progressPercent === 100 && (
              <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-[#E6ECE5] dark:bg-[#222B22] text-[#516351] dark:text-[#7B947B]">
                ✓ All taken
              </span>
            )}
          </div>
        </div>
      )}

      {/* Next Up Banner */}
      {nextPendingDose && (
        <div className="mb-6 p-4 rounded-2xl bg-[#E6ECE5]/80 dark:bg-[#222B22]/80 border border-[#516351]/20 dark:border-[#7B947B]/20 shadow-xs flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-full bg-[#516351] dark:bg-[#7B947B] flex items-center justify-center text-white dark:text-[#0E130E]">
              <Clock size={18} />
            </div>
            <div>
              <span className="text-[10px] uppercase font-bold tracking-wider text-[#516351] dark:text-[#7B947B]">
                NEXT UP · {formatTimeSlot(nextPendingDose.log.scheduledSlot)}
              </span>
              <h4 className="text-sm font-semibold text-[#141613] dark:text-[#EDEDEA]">
                {nextPendingDose.med.name}
              </h4>
              <p className="text-xs text-[#5E645A] dark:text-[#A3A89F]">
                {nextPendingDose.med.dosage}
              </p>
            </div>
          </div>

          <button
            onClick={() => handleTakeDose(nextPendingDose.log.id)}
            className="px-4 py-2 text-xs font-semibold rounded-xl bg-[#516351] dark:bg-[#7B947B] text-white dark:text-[#0E130E] hover:opacity-90 active:scale-95 transition-all shadow-xs cursor-pointer"
          >
            Take now
          </button>
        </div>
      )}

      {/* Doses Section */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <span className="text-[11px] font-semibold tracking-[0.2em] text-[#8C9287] dark:text-[#73796E] uppercase">
            DOSES ({dayDoseLogs.length})
          </span>
          {medications.length > 0 && (
            <button
              onClick={onAddMedication}
              className="text-xs font-medium text-[#516351] dark:text-[#7B947B] flex items-center gap-1 hover:underline cursor-pointer"
            >
              <Plus size={14} />
              <span>Add medication</span>
            </button>
          )}
        </div>

        {dayDoseLogs.length === 0 ? (
          <div className="py-12 px-6 text-center rounded-2xl bg-[#FFFFFF] dark:bg-[#1C1F1A] border border-[#E2E1D9] dark:border-[#292E26] flex flex-col items-center">
            <HavnBreathingMark size={56} className="mb-4" />
            <h3 className="text-base font-semibold text-[#141613] dark:text-[#EDEDEA]">
              No doses scheduled
            </h3>
            <p className="text-xs text-[#8C9287] dark:text-[#73796E] mt-1.5 max-w-xs leading-relaxed">
              {medications.length === 0
                ? 'Add the things you take. Hävn keeps the schedule and records your adherence quietly.'
                : 'No reminders or scheduled doses for this day.'}
            </p>
            <div className="mt-5">
              <HavnButton
                text="Add a medication"
                onClick={onAddMedication}
                size="medium"
                fillWidth={false}
              />
            </div>
          </div>
        ) : (
          <div className="space-y-3">
            <AnimatePresence mode="popLayout">
              {dayDoseLogs.map(({ log, med }) => {
                const accent = medAccent(med.colorTag);
                const isTaken = log.status === 'TAKEN';
                const isSkipped = log.status === 'SKIPPED';
                const isPending = log.status === 'PENDING' || log.status === 'SNOOZED';

                return (
                  <motion.div
                    key={log.id}
                    layout
                    initial={{ opacity: 0, y: 8 }}
                    animate={{ opacity: 1, y: 0 }}
                    exit={{ opacity: 0, scale: 0.95 }}
                    transition={{ duration: 0.2 }}
                    className={`p-4 rounded-2xl border transition-all duration-200 ${
                      isTaken
                        ? 'bg-[#F4F4F0]/60 dark:bg-[#171A15]/60 border-[#E2E1D9]/70 dark:border-[#282E25]/70 opacity-80'
                        : isSkipped
                        ? 'bg-[#ECEBE4]/40 dark:bg-[#1C1F1A]/40 border-[#E2E1D9] dark:border-[#292E26] opacity-60'
                        : 'bg-[#FFFFFF] dark:bg-[#1C1F1A] border-[#E2E1D9] dark:border-[#292E26] shadow-xs'
                    }`}
                  >
                    <div className="flex items-center justify-between gap-3">
                      {/* Left icon & details */}
                      <div className="flex items-center gap-3.5 flex-1 min-w-0">
                        <div
                          style={{
                            backgroundColor: isTaken ? `${accent}25` : `${accent}18`,
                            borderColor: isTaken ? `${accent}40` : 'transparent',
                          }}
                          className="w-11 h-11 rounded-xl flex items-center justify-center shrink-0 border"
                        >
                          <MedIcon type={med.iconType} size={22} color={accent} />
                        </div>

                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-2">
                            <h4
                              className={`text-sm font-semibold truncate ${
                                isTaken
                                  ? 'line-through text-[#5E645A] dark:text-[#A3A89F]'
                                  : 'text-[#141613] dark:text-[#EDEDEA]'
                              }`}
                            >
                              {med.name}
                            </h4>
                            <span className="text-[11px] font-medium text-[#8C9287] dark:text-[#73796E] shrink-0">
                              · {formatTimeSlot(log.scheduledSlot)}
                            </span>
                          </div>

                          <p className="text-xs text-[#8C9287] dark:text-[#73796E] truncate mt-0.5">
                            {med.dosage}
                            {med.notes && ` · ${med.notes}`}
                          </p>

                          {isTaken && log.takenAt && (
                            <span className="text-[10px] font-medium text-[#516351] dark:text-[#7B947B] flex items-center gap-1 mt-1">
                              <Check size={11} strokeWidth={3} />
                              Taken at {new Date(log.takenAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                            </span>
                          )}

                          {isSkipped && (
                            <span className="text-[10px] font-medium text-[#9A5637] dark:text-[#D97A52] mt-1">
                              Skipped
                            </span>
                          )}
                        </div>
                      </div>

                      {/* Right action buttons */}
                      <div className="flex items-center gap-1.5 shrink-0">
                        {isPending && (
                          <>
                            <button
                              onClick={() => handleSkipDose(log.id)}
                              title="Skip dose"
                              className="w-9 h-9 rounded-xl flex items-center justify-center text-[#8C9287] hover:text-[#9A5637] hover:bg-[#ECEBE4] dark:hover:bg-[#282E25] transition-colors cursor-pointer"
                            >
                              <X size={16} />
                            </button>
                            <button
                              onClick={() => handleTakeDose(log.id)}
                              className="px-3.5 py-2 rounded-xl bg-[#516351] dark:bg-[#7B947B] text-white dark:text-[#0E130E] text-xs font-semibold flex items-center gap-1.5 hover:opacity-95 active:scale-95 transition-all shadow-xs cursor-pointer"
                            >
                              <Check size={14} strokeWidth={3} />
                              <span>Take</span>
                            </button>
                          </>
                        )}

                        {isTaken && (
                          <button
                            onClick={() => handleUndoDose(log.id)}
                            title="Undo taken status"
                            className="px-3 py-1.5 rounded-lg text-xs font-medium text-[#8C9287] dark:text-[#73796E] hover:text-[#141613] dark:hover:text-[#EDEDEA] hover:bg-[#ECEBE4] dark:hover:bg-[#282E25] flex items-center gap-1 transition-colors cursor-pointer"
                          >
                            <RotateCcw size={12} />
                            <span>Undo</span>
                          </button>
                        )}

                        {isSkipped && (
                          <button
                            onClick={() => handleUndoDose(log.id)}
                            title="Reset status"
                            className="px-3 py-1.5 rounded-lg text-xs font-medium text-[#8C9287] dark:text-[#73796E] hover:text-[#141613] dark:hover:text-[#EDEDEA] hover:bg-[#ECEBE4] dark:hover:bg-[#282E25] flex items-center gap-1 transition-colors cursor-pointer"
                          >
                            <RotateCcw size={12} />
                            <span>Undo</span>
                          </button>
                        )}
                      </div>
                    </div>
                  </motion.div>
                );
              })}
            </AnimatePresence>
          </div>
        )}
      </div>
    </div>
  );
};
