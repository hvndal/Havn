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

  // Spring physics constants matching luxury motion choreography
  const springLuxury = { type: 'spring', stiffness: 120, damping: 16, mass: 0.8 } as const;
  const springSnappy = { type: 'spring', stiffness: 340, damping: 24, mass: 0.6 } as const;

  return (
    <div className="w-full max-w-xl mx-auto px-4 pb-28 pt-4">
      {/* Header */}
      <motion.div
        initial={{ opacity: 0, y: 12, filter: 'blur(4px)' }}
        animate={{ opacity: 1, y: 0, filter: 'blur(0px)' }}
        transition={{ ...springLuxury, delay: 0.04 }}
        className="mb-6"
      >
        <div className="flex items-center justify-between">
          <div>
            <div className="flex items-center gap-2">
              <span className="w-1.5 h-1.5 rounded-full bg-[#516351] dark:bg-[#7B947B] opacity-80" />
              <span className="text-[11px] font-semibold tracking-[0.22em] text-[#7C8276] dark:text-[#888E82] uppercase">
                {isToday ? "TODAY'S SCHEDULE" : selectedDate.toLocaleDateString(undefined, { weekday: 'long', month: 'short', day: 'numeric' }).toUpperCase()}
              </span>
            </div>
            <h1 className="text-2xl font-bold text-[#171A15] dark:text-[#EDEDEA] mt-1 font-sans tracking-tight">
              {isToday ? 'Today' : selectedDate.toLocaleDateString(undefined, { month: 'short', day: 'numeric' })}
            </h1>
          </div>

          {/* User profile avatar pill */}
          {user && (
            <motion.div
              onClick={onOpenManageMeds}
              whileHover={{ scale: 1.03, y: -1 }}
              whileTap={{ scale: 0.96 }}
              transition={springSnappy}
              className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-[#F5F4EE] dark:bg-[#1C201A] border border-[#141613]/[0.07] dark:border-white/[0.08] shadow-[0_2px_8px_rgba(20,22,19,0.03)] cursor-pointer"
            >
              <div
                style={{ backgroundColor: user.avatarColor }}
                className="w-5 h-5 rounded-full flex items-center justify-center text-[10px] text-white font-bold shadow-xs"
              >
                {user.name[0]?.toUpperCase()}
              </div>
              <span className="text-xs font-medium text-[#4D5348] dark:text-[#A7ADA3]">
                {user.name}
              </span>
            </motion.div>
          )}
        </div>

        {/* 7-Day Horizontal Date Strip */}
        <div className="flex items-center justify-between mt-5 gap-1.5 overflow-x-auto pb-1">
          {dateStrip.map((d, index) => {
            const isSelected =
              d.getDate() === selectedDate.getDate() &&
              d.getMonth() === selectedDate.getMonth();
            const isCurrToday =
              d.getDate() === new Date().getDate() &&
              d.getMonth() === new Date().getMonth();

            return (
              <motion.button
                key={d.toISOString()}
                initial={{ opacity: 0, y: 14, filter: 'blur(3px)' }}
                animate={{ opacity: 1, y: 0, filter: 'blur(0px)' }}
                transition={{ ...springLuxury, delay: 0.08 + index * 0.035 }}
                whileHover={{ scale: 1.04, y: -1 }}
                whileTap={{ scale: 0.94 }}
                onClick={() => {
                  soundManager.playSoftTap();
                  onSelectDate(d);
                }}
                className={`flex-1 min-w-[42px] py-2.5 rounded-[16px] flex flex-col items-center justify-center cursor-pointer select-none border relative overflow-hidden transition-colors ${
                  isSelected
                    ? 'bg-[#4E614E] dark:bg-[#7B947B] text-[#FAF8F5] dark:text-[#0C120C] border-[#415341]/60 dark:border-[#8BA58B]/50 shadow-[0_8px_20px_-4px_rgba(78,97,78,0.36),0_2px_6px_rgba(78,97,78,0.18)]'
                    : 'bg-[#F6F5F0] dark:bg-[#1A1D17] text-[#555C50] dark:text-[#9EA497] border-[#141613]/[0.06] dark:border-white/[0.07] hover:bg-[#EEECE4] dark:hover:bg-[#232820] shadow-[0_2px_6px_rgba(0,0,0,0.02)]'
                }`}
              >
                <span className="text-[10px] uppercase font-semibold tracking-wider opacity-75">
                  {d.toLocaleDateString(undefined, { weekday: 'narrow' })}
                </span>
                <span className="text-sm font-bold mt-0.5 tracking-tight">{d.getDate()}</span>
                {isCurrToday && (
                  <span
                    className={`w-1.5 h-0.5 rounded-full mt-1.5 ${
                      isSelected ? 'bg-[#FAF8F5] dark:bg-[#0C120C]' : 'bg-[#4E614E] dark:bg-[#7B947B]'
                    }`}
                  />
                )}
              </motion.button>
            );
          })}
        </div>
      </motion.div>

      {/* Daily Progress Card */}
      {totalCount > 0 && (
        <motion.div
          initial={{ opacity: 0, y: 16, filter: 'blur(4px)' }}
          animate={{ opacity: 1, y: 0, filter: 'blur(0px)' }}
          transition={{ ...springLuxury, delay: 0.28 }}
          whileHover={{ y: -1.5, transition: springSnappy }}
          className="mb-6 p-4.5 rounded-[22px] bg-[#FFFFFF] dark:bg-[#1A1D18] border border-[#141613]/[0.06] dark:border-white/[0.07] shadow-[0_8px_30px_rgba(0,0,0,0.035),0_1px_3px_rgba(0,0,0,0.02)] relative overflow-hidden backdrop-blur-xs"
        >
          {/* Subtle microscopic top highlight line */}
          <div className="absolute inset-x-0 top-0 h-[1px] bg-gradient-to-r from-transparent via-black/[0.05] dark:via-white/[0.1] to-transparent pointer-events-none" />

          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3.5">
              <div className="relative w-12 h-12 flex items-center justify-center">
                <svg className="w-12 h-12 transform -rotate-90">
                  <circle
                    cx="24"
                    cy="24"
                    r="20"
                    stroke="currentColor"
                    strokeWidth="3.5"
                    fill="transparent"
                    className="text-[#EFECE5] dark:text-[#252B21]"
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
                    className="text-[#4E614E] dark:text-[#7B947B] transition-all duration-700 ease-out"
                  />
                </svg>
                <span className="absolute text-xs font-bold text-[#171A15] dark:text-[#EDEDEA] tracking-tight">
                  {progressPercent}%
                </span>
              </div>

              <div>
                <h3 className="text-sm font-semibold text-[#171A15] dark:text-[#EDEDEA] tracking-tight">
                  {takenCount} of {totalCount} completed
                </h3>
                <p className="text-xs text-[#7A8174] dark:text-[#888E83] mt-0.5">
                  {progressPercent === 100
                    ? 'All doses recorded for this day'
                    : `${totalCount - takenCount} remaining today`}
                </p>
              </div>
            </div>

            {progressPercent === 100 && (
              <motion.span
                initial={{ scale: 0.9, opacity: 0 }}
                animate={{ scale: 1, opacity: 1 }}
                transition={springSnappy}
                className="px-3 py-1.5 text-xs font-semibold rounded-full bg-[#E5ECE4] dark:bg-[#1F291F] text-[#425442] dark:text-[#88A388] border border-[#516351]/15 dark:border-[#7B947B]/20 shadow-[0_2px_8px_rgba(81,99,81,0.08)] flex items-center gap-1.5"
              >
                <span className="w-1.5 h-1.5 rounded-full bg-[#4E614E] dark:bg-[#7B947B]" />
                All taken
              </motion.span>
            )}
          </div>
        </motion.div>
      )}

      {/* Next Up Banner */}
      {nextPendingDose && (
        <motion.div
          initial={{ opacity: 0, y: 14, filter: 'blur(4px)' }}
          animate={{ opacity: 1, y: 0, filter: 'blur(0px)' }}
          transition={{ ...springLuxury, delay: 0.34 }}
          whileHover={{ y: -1.5, transition: springSnappy }}
          className="mb-6 p-4.5 rounded-[22px] bg-[#EBF1EA]/90 dark:bg-[#1B241B]/85 border border-[#4E614E]/20 dark:border-[#7B947B]/25 shadow-[0_8px_26px_rgba(78,97,78,0.08),0_1px_3px_rgba(0,0,0,0.02)] flex items-center justify-between backdrop-blur-xs relative overflow-hidden"
        >
          {/* Subtle microscopic top highlight line */}
          <div className="absolute inset-x-0 top-0 h-[1px] bg-gradient-to-r from-transparent via-[#4E614E]/20 dark:via-[#7B947B]/30 to-transparent pointer-events-none" />

          <div className="flex items-center gap-3.5">
            <div className="w-11 h-11 rounded-[16px] bg-[#4E614E] dark:bg-[#7B947B] flex items-center justify-center text-[#FAF8F5] dark:text-[#0C120C] shadow-[0_4px_12px_rgba(78,97,78,0.25)] shrink-0">
              <Clock size={19} />
            </div>
            <div>
              <div className="flex items-center gap-1.5">
                <span className="w-1.5 h-1.5 rounded-full bg-[#4E614E] dark:bg-[#7B947B] animate-pulse" />
                <span className="text-[10px] uppercase font-bold tracking-wider text-[#4E614E] dark:text-[#7B947B]">
                  NEXT UP · {formatTimeSlot(nextPendingDose.log.scheduledSlot)}
                </span>
              </div>
              <h4 className="text-sm font-semibold text-[#171A15] dark:text-[#EDEDEA] mt-0.5 tracking-tight">
                {nextPendingDose.med.name}
              </h4>
              <p className="text-xs text-[#5C6357] dark:text-[#A1A79C]">
                {nextPendingDose.med.dosage}
              </p>
            </div>
          </div>

          <motion.button
            whileHover={{ scale: 1.03 }}
            whileTap={{ scale: 0.94 }}
            transition={springSnappy}
            onClick={() => handleTakeDose(nextPendingDose.log.id)}
            className="px-4 py-2.5 text-xs font-semibold rounded-[14px] bg-[#4E614E] dark:bg-[#7B947B] text-[#FAF8F5] dark:text-[#0C120C] shadow-[0_4px_14px_rgba(78,97,78,0.26)] hover:shadow-[0_6px_18px_rgba(78,97,78,0.36)] cursor-pointer"
          >
            Take now
          </motion.button>
        </motion.div>
      )}

      {/* Doses Section */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <span className="text-[11px] font-bold tracking-[0.22em] text-[#7A8174] dark:text-[#888E83] uppercase">
            DOSES ({dayDoseLogs.length})
          </span>
          {medications.length > 0 && (
            <motion.button
              whileHover={{ scale: 1.04 }}
              whileTap={{ scale: 0.96 }}
              transition={springSnappy}
              onClick={onAddMedication}
              className="text-xs font-medium text-[#4E614E] dark:text-[#7B947B] flex items-center gap-1 cursor-pointer hover:underline"
            >
              <Plus size={14} />
              <span>Add medication</span>
            </motion.button>
          )}
        </div>

        {dayDoseLogs.length === 0 ? (
          <motion.div
            initial={{ opacity: 0, y: 16, filter: 'blur(4px)' }}
            animate={{ opacity: 1, y: 0, filter: 'blur(0px)' }}
            transition={springLuxury}
            className="py-12 px-6 text-center rounded-[22px] bg-[#FFFFFF] dark:bg-[#1A1D18] border border-[#141613]/[0.06] dark:border-white/[0.07] flex flex-col items-center shadow-[0_8px_30px_rgba(0,0,0,0.035),0_1px_3px_rgba(0,0,0,0.02)]"
          >
            <HavnBreathingMark size={56} className="mb-4" />
            <h3 className="text-base font-semibold text-[#171A15] dark:text-[#EDEDEA] tracking-tight">
              No doses scheduled
            </h3>
            <p className="text-xs text-[#7A8174] dark:text-[#888E83] mt-1.5 max-w-xs leading-relaxed">
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
          </motion.div>
        ) : (
          <div className="space-y-3">
            <AnimatePresence mode="popLayout">
              {dayDoseLogs.map(({ log, med }, index) => {
                const accent = medAccent(med.colorTag);
                const isTaken = log.status === 'TAKEN';
                const isSkipped = log.status === 'SKIPPED';
                const isPending = log.status === 'PENDING' || log.status === 'SNOOZED';

                return (
                  <motion.div
                    key={log.id}
                    layout
                    initial={{ opacity: 0, y: 16, filter: 'blur(4px)' }}
                    animate={{ opacity: 1, y: 0, filter: 'blur(0px)' }}
                    exit={{ opacity: 0, scale: 0.94, filter: 'blur(2px)' }}
                    transition={{ ...springLuxury, delay: 0.38 + index * 0.045 }}
                    whileHover={{ y: -1.5, transition: springSnappy }}
                    className={`p-4.5 rounded-[20px] border transition-all duration-300 relative overflow-hidden ${
                      isTaken
                        ? 'bg-[#F5F4EE]/70 dark:bg-[#161814]/70 border-[#141613]/[0.05] dark:border-white/[0.05] opacity-80 shadow-[0_2px_8px_rgba(0,0,0,0.01)]'
                        : isSkipped
                        ? 'bg-[#EFECE5]/50 dark:bg-[#1A1D17]/50 border-[#141613]/[0.05] dark:border-white/[0.05] opacity-65'
                        : 'bg-[#FCFBF8] dark:bg-[#1A1E18] border-[#141613]/[0.06] dark:border-white/[0.07] shadow-[0_6px_22px_rgba(0,0,0,0.035),0_1px_3px_rgba(0,0,0,0.02)]'
                    }`}
                  >
                    {/* Microscopic top highlight */}
                    {!isTaken && !isSkipped && (
                      <div className="absolute inset-x-0 top-0 h-[1px] bg-gradient-to-r from-transparent via-black/[0.04] dark:via-white/[0.08] to-transparent pointer-events-none" />
                    )}

                    <div className="flex items-center justify-between gap-3">
                      {/* Left icon & details */}
                      <div className="flex items-center gap-3.5 flex-1 min-w-0">
                        <div
                          style={{
                            backgroundColor: isTaken ? `${accent}22` : `${accent}16`,
                            borderColor: isTaken ? `${accent}38` : `${accent}20`,
                          }}
                          className="w-12 h-12 rounded-[16px] flex items-center justify-center shrink-0 border transition-all duration-200 shadow-xs"
                        >
                          <MedIcon type={med.iconType} size={22} color={accent} />
                        </div>

                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-2">
                            <h4
                              className={`text-sm font-semibold truncate tracking-tight ${
                                isTaken
                                  ? 'line-through text-[#5C6357] dark:text-[#A1A79C]'
                                  : 'text-[#171A15] dark:text-[#EDEDEA]'
                              }`}
                            >
                              {med.name}
                            </h4>
                            <span className="text-[11px] font-medium text-[#7A8174] dark:text-[#888E83] shrink-0">
                              · {formatTimeSlot(log.scheduledSlot)}
                            </span>
                          </div>

                          <p className="text-xs text-[#7A8174] dark:text-[#888E83] truncate mt-0.5">
                            {med.dosage}
                            {med.notes && ` · ${med.notes}`}
                          </p>

                          {isTaken && log.takenAt && (
                            <span className="text-[10px] font-medium text-[#4E614E] dark:text-[#7B947B] flex items-center gap-1 mt-1">
                              <Check size={11} strokeWidth={3} />
                              Taken at {new Date(log.takenAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                            </span>
                          )}

                          {isSkipped && (
                            <span className="text-[10px] font-medium text-[#9A5637] dark:text-[#D97A52] mt-1 flex items-center gap-1">
                              <span className="w-1 h-1 rounded-full bg-[#9A5637] dark:bg-[#D97A52]" />
                              Skipped
                            </span>
                          )}
                        </div>
                      </div>

                      {/* Right action buttons */}
                      <div className="flex items-center gap-1.5 shrink-0">
                        {isPending && (
                          <>
                            <motion.button
                              whileHover={{ scale: 1.08 }}
                              whileTap={{ scale: 0.92 }}
                              transition={springSnappy}
                              onClick={() => handleSkipDose(log.id)}
                              title="Skip dose"
                              className="w-9.5 h-9.5 rounded-[12px] flex items-center justify-center text-[#7A8174] hover:text-[#9A5637] hover:bg-[#EFECE5] dark:hover:bg-[#252B21] transition-colors cursor-pointer"
                            >
                              <X size={16} />
                            </motion.button>
                            <motion.button
                              whileHover={{ scale: 1.04 }}
                              whileTap={{ scale: 0.94 }}
                              transition={springSnappy}
                              onClick={() => handleTakeDose(log.id)}
                              className="px-4 py-2.5 rounded-[14px] bg-[#4E614E] dark:bg-[#7B947B] text-[#FAF8F5] dark:text-[#0C120C] text-xs font-semibold flex items-center gap-1.5 shadow-[0_4px_14px_rgba(78,97,78,0.25)] hover:shadow-[0_6px_18px_rgba(78,97,78,0.35)] cursor-pointer"
                            >
                              <Check size={14} strokeWidth={3} />
                              <span>Take</span>
                            </motion.button>
                          </>
                        )}

                        {isTaken && (
                          <motion.button
                            whileHover={{ scale: 1.04 }}
                            whileTap={{ scale: 0.95 }}
                            transition={springSnappy}
                            onClick={() => handleUndoDose(log.id)}
                            title="Undo taken status"
                            className="px-3.5 py-1.5 rounded-[10px] text-xs font-medium text-[#7A8174] dark:text-[#888E83] hover:text-[#171A15] dark:hover:text-[#EDEDEA] hover:bg-[#EFECE5] dark:hover:bg-[#252B21] flex items-center gap-1 transition-colors cursor-pointer border border-transparent hover:border-[#141613]/[0.06] dark:hover:border-white/[0.07]"
                          >
                            <RotateCcw size={12} />
                            <span>Undo</span>
                          </motion.button>
                        )}

                        {isSkipped && (
                          <motion.button
                            whileHover={{ scale: 1.04 }}
                            whileTap={{ scale: 0.95 }}
                            transition={springSnappy}
                            onClick={() => handleUndoDose(log.id)}
                            title="Reset status"
                            className="px-3.5 py-1.5 rounded-[10px] text-xs font-medium text-[#7A8174] dark:text-[#888E83] hover:text-[#171A15] dark:hover:text-[#EDEDEA] hover:bg-[#EFECE5] dark:hover:bg-[#252B21] flex items-center gap-1 transition-colors cursor-pointer border border-transparent hover:border-[#141613]/[0.06] dark:hover:border-white/[0.07]"
                          >
                            <RotateCcw size={12} />
                            <span>Undo</span>
                          </motion.button>
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
