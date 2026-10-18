import React, { useState, useMemo } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { Check, CheckCheck, Clock, RotateCcw } from 'lucide-react';
import { Medication, DoseLog, DoseStatus, OrganizerSlotData, SlotLabel } from '../types';
import { Organizer3D } from '../components/Organizer3D';
import { MedIcon } from '../components/MedIcon';
import { medAccent } from '../theme/tokens';
import { soundManager } from '../audio/soundManager';

interface OrganizerScreenProps {
  medications: Medication[];
  doseLogs: DoseLog[];
  selectedDate: Date;
  onSelectDate: (date: Date) => void;
  onUpdateDoseStatus: (logId: number, status: DoseStatus) => void;
  isDark: boolean;
}

const SLOT_ORDER: { label: SlotLabel; period: string; startHour: number; endHour: number }[] = [
  { label: 'MORNING', period: '05:00 - 11:59', startHour: 5, endHour: 12 },
  { label: 'AFTERNOON', period: '12:00 - 16:59', startHour: 12, endHour: 17 },
  { label: 'EVENING', period: '17:00 - 20:59', startHour: 17, endHour: 21 },
  { label: 'NIGHT', period: '21:00 - 04:59', startHour: 21, endHour: 5 },
];

export const OrganizerScreen: React.FC<OrganizerScreenProps> = ({
  medications,
  doseLogs,
  selectedDate,
  onSelectDate,
  onUpdateDoseStatus,
  isDark,
}) => {
  const [selectedSlotIndex, setSelectedSlotIndex] = useState<number>(0);

  // Filter logs for this day
  const dayLogs = useMemo(() => {
    const start = new Date(selectedDate);
    start.setHours(0, 0, 0, 0);
    const end = new Date(selectedDate);
    end.setHours(23, 59, 59, 999);

    return doseLogs.filter(
      (l) => l.scheduledTime >= start.getTime() && l.scheduledTime <= end.getTime()
    );
  }, [selectedDate, doseLogs]);

  // Map 4 Slots Data for 3D Pill Box
  const slotDataList = useMemo<OrganizerSlotData[]>(() => {
    const currentHour = new Date().getHours();

    return SLOT_ORDER.map((slot, idx) => {
      // Find doses belonging to this time range
      const matchingDoses = dayLogs
        .map((log) => {
          const med = medications.find((m) => m.id === log.medicationId);
          return { log, med };
        })
        .filter((item): item is { log: DoseLog; med: Medication } => {
          if (!item.med) return false;
          const d = new Date(item.log.scheduledTime);
          const hour = d.getHours();
          if (slot.startHour < slot.endHour) {
            return hour >= slot.startHour && hour < slot.endHour;
          } else {
            // Night spans midnight (21 to 5)
            return hour >= slot.startHour || hour < slot.endHour;
          }
        });

      const pills = matchingDoses.map(({ log, med }) => {
        const accent = medAccent(med.colorTag);
        let shape: 'capsule' | 'tablet' | 'softgel' = 'capsule';
        if (med.iconType === 'TABLET' || med.iconType === 'PATCH') shape = 'tablet';
        else if (med.iconType === 'DROPS' || med.iconType === 'LIQUID') shape = 'softgel';

        return {
          id: `pill-${log.id}`,
          medicationId: med.id,
          doseLogId: log.id,
          name: med.name,
          color: accent,
          secondaryColor: '#FFFFFF',
          shape,
          isTaken: log.status === 'TAKEN',
        };
      });

      const isCompleted = pills.length > 0 && pills.every((p) => p.isTaken);
      const isCurrent =
        slot.startHour < slot.endHour
          ? currentHour >= slot.startHour && currentHour < slot.endHour
          : currentHour >= slot.startHour || currentHour < slot.endHour;

      return {
        label: slot.label,
        period: slot.period,
        isCompleted,
        isCurrent,
        pills,
      };
    });
  }, [dayLogs, medications]);

  const activeSlot = slotDataList[selectedSlotIndex] || slotDataList[0];

  const handleTakeAllInSlot = () => {
    soundManager.playSoftChime();
    activeSlot.pills.forEach((p) => {
      if (!p.isTaken) {
        onUpdateDoseStatus(p.doseLogId, 'TAKEN');
      }
    });
  };

  return (
    <div className="w-full max-w-xl mx-auto px-4 pb-28 pt-4 flex flex-col h-[calc(100vh-80px)]">
      {/* Header */}
      <div className="flex items-center justify-between mb-3">
        <div>
          <span className="text-[11px] font-semibold tracking-[0.2em] text-[#8C9287] dark:text-[#73796E] uppercase">
            3D PILL ORGANISER
          </span>
          <h1 className="text-2xl font-bold text-[#141613] dark:text-[#EDEDEA] font-sans">
            {selectedDate.toLocaleDateString(undefined, { weekday: 'long' })}
          </h1>
        </div>

        {/* Quick Date Switcher */}
        <div className="flex items-center gap-1 bg-[#ECEBE4] dark:bg-[#1C1F1A] p-1 rounded-xl border border-[#E2E1D9] dark:border-[#292E26]">
          {[-1, 0, 1].map((offset) => {
            const d = new Date();
            d.setDate(d.getDate() + offset);
            const isSelected =
              d.getDate() === selectedDate.getDate() &&
              d.getMonth() === selectedDate.getMonth();
            const label = offset === -1 ? 'Yest' : offset === 0 ? 'Today' : 'Tmrw';

            return (
              <button
                key={label}
                onClick={() => {
                  soundManager.playSoftTap();
                  onSelectDate(d);
                }}
                className={`px-2.5 py-1 text-xs font-semibold rounded-lg transition-all cursor-pointer ${
                  isSelected
                    ? 'bg-[#516351] dark:bg-[#7B947B] text-white dark:text-[#0E130E] shadow-2xs'
                    : 'text-[#8C9287] dark:text-[#73796E] hover:text-[#141613] dark:hover:text-[#EDEDEA]'
                }`}
              >
                {label}
              </button>
            );
          })}
        </div>
      </div>

      {/* 3D Canvas Stage */}
      <div className="flex-1 min-h-[260px] max-h-[380px] w-full rounded-2xl bg-[#ECEBE4]/50 dark:bg-[#181B16]/80 border border-[#E2E1D9] dark:border-[#292E26] overflow-hidden relative shadow-inner">
        <Organizer3D
          slots={slotDataList}
          selectedSlotIndex={selectedSlotIndex}
          onSelectSlot={(idx) => setSelectedSlotIndex(idx)}
          isDark={isDark}
        />
      </div>

      {/* Compartment Selector Tabs */}
      <div className="grid grid-cols-4 gap-2 mt-4">
        {slotDataList.map((slot, idx) => {
          const isSelected = idx === selectedSlotIndex;
          const isDone = slot.isCompleted;

          return (
            <button
              key={slot.label}
              onClick={() => {
                soundManager.playCeramicClick();
                setSelectedSlotIndex(idx);
              }}
              className={`py-2.5 px-2 rounded-xl text-center border transition-all cursor-pointer select-none flex flex-col items-center justify-center ${
                isSelected
                  ? 'bg-[#516351] dark:bg-[#7B947B] text-white dark:text-[#0E130E] border-[#516351] dark:border-[#7B947B] shadow-xs'
                  : isDone
                  ? 'bg-[#E6ECE5] dark:bg-[#222B22] text-[#516351] dark:text-[#7B947B] border-[#516351]/30 dark:border-[#7B947B]/30'
                  : 'bg-[#FFFFFF] dark:bg-[#1C1F1A] text-[#5E645A] dark:text-[#A3A89F] border-[#E2E1D9] dark:border-[#292E26] hover:bg-[#ECEBE4] dark:hover:bg-[#282E25]'
              }`}
            >
              <span className="text-[10px] font-bold tracking-wider uppercase">
                {slot.label.slice(0, 4)}
              </span>
              <span className="text-[11px] font-medium mt-0.5 opacity-80">
                {isDone ? '✓ Done' : `${slot.pills.length} p.`}
              </span>
            </button>
          );
        })}
      </div>

      {/* Selected Compartment Detail Card */}
      <div className="mt-4 p-4 rounded-2xl bg-[#FFFFFF] dark:bg-[#1C1F1A] border border-[#E2E1D9] dark:border-[#292E26] shadow-xs flex-1 overflow-y-auto">
        <div className="flex items-center justify-between border-b border-[#E2E1D9] dark:border-[#292E26] pb-3 mb-3">
          <div>
            <span className="text-[10px] uppercase font-bold tracking-wider text-[#8C9287] dark:text-[#73796E]">
              COMPARTMENT · {activeSlot.period}
            </span>
            <h3 className="text-base font-bold text-[#141613] dark:text-[#EDEDEA]">
              {activeSlot.label}
            </h3>
          </div>

          {activeSlot.pills.length > 0 && !activeSlot.isCompleted && (
            <button
              onClick={handleTakeAllInSlot}
              className="px-3 py-1.5 rounded-xl bg-[#516351] dark:bg-[#7B947B] text-white dark:text-[#0E130E] text-xs font-semibold flex items-center gap-1.5 hover:opacity-90 active:scale-95 transition-all shadow-xs cursor-pointer"
            >
              <CheckCheck size={14} />
              <span>Take all</span>
            </button>
          )}

          {activeSlot.isCompleted && (
            <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-[#E6ECE5] dark:bg-[#222B22] text-[#516351] dark:text-[#7B947B] flex items-center gap-1">
              <Check size={12} strokeWidth={3} />
              Completed
            </span>
          )}
        </div>

        {activeSlot.pills.length === 0 ? (
          <div className="py-6 text-center text-xs text-[#8C9287] dark:text-[#73796E]">
            No medications scheduled for this slot.
          </div>
        ) : (
          <div className="space-y-2">
            {activeSlot.pills.map((pill) => {
              const med = medications.find((m) => m.id === pill.medicationId);
              return (
                <div
                  key={pill.id}
                  className={`flex items-center justify-between p-2.5 rounded-xl border transition-all ${
                    pill.isTaken
                      ? 'bg-[#ECEBE4]/50 dark:bg-[#181B16]/50 border-transparent opacity-75'
                      : 'bg-[#ECEBE4] dark:bg-[#242822] border-[#E2E1D9] dark:border-[#292E26]'
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <div
                      style={{ backgroundColor: `${pill.color}30` }}
                      className="w-8 h-8 rounded-lg flex items-center justify-center shrink-0"
                    >
                      {med && <MedIcon type={med.iconType} size={16} color={pill.color} />}
                    </div>
                    <div>
                      <h5
                        className={`text-xs font-semibold ${
                          pill.isTaken
                            ? 'line-through text-[#8C9287] dark:text-[#73796E]'
                            : 'text-[#141613] dark:text-[#EDEDEA]'
                        }`}
                      >
                        {pill.name}
                      </h5>
                      {med && (
                        <p className="text-[11px] text-[#8C9287] dark:text-[#73796E]">
                          {med.dosage}
                        </p>
                      )}
                    </div>
                  </div>

                  {pill.isTaken ? (
                    <button
                      onClick={() => {
                        soundManager.playSoftTap();
                        onUpdateDoseStatus(pill.doseLogId, 'PENDING');
                      }}
                      className="text-xs text-[#8C9287] hover:text-[#141613] dark:hover:text-[#EDEDEA] flex items-center gap-1 px-2 py-1 rounded cursor-pointer"
                    >
                      <RotateCcw size={11} />
                      <span>Undo</span>
                    </button>
                  ) : (
                    <button
                      onClick={() => {
                        soundManager.playSoftChime();
                        onUpdateDoseStatus(pill.doseLogId, 'TAKEN');
                      }}
                      className="px-3 py-1.5 rounded-lg bg-[#516351] dark:bg-[#7B947B] text-white dark:text-[#0E130E] text-xs font-semibold flex items-center gap-1 hover:opacity-90 active:scale-95 transition-all shadow-xs cursor-pointer"
                    >
                      <Check size={12} strokeWidth={3} />
                      <span>Take</span>
                    </button>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};
