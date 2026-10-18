import React, { useState } from 'react';
import { ArrowLeft, Bell, BellOff, Check, Clock, Volume2, AlertCircle } from 'lucide-react';
import { Medication, UserPreferences } from '../types';
import { HavnIconButton, HavnButton } from '../components/HavnButton';
import { HavnSwitch, HavnSwitchRow } from '../components/HavnSwitch';
import { soundManager } from '../audio/soundManager';

interface RemindersScreenProps {
  medications: Medication[];
  preferences: UserPreferences;
  onUpdatePreferences: (prefs: Partial<UserPreferences>) => void;
  onBack: () => void;
}

export const RemindersScreen: React.FC<RemindersScreenProps> = ({
  medications,
  preferences,
  onUpdatePreferences,
  onBack,
}) => {
  const [testSent, setTestSent] = useState(false);

  // Unscheduled medications
  const unscheduledMeds = medications.filter(
    (m) => m.isActive && m.repeatType !== 'AS_NEEDED' && m.reminderTimes.length === 0
  );

  // All reminder times flattened and sorted
  const upcomingTimes = Array.from(
    new Set(
      medications
        .filter((m) => m.isActive)
        .flatMap((m) => m.reminderTimes)
    )
  ).sort();

  const handleTestReminder = () => {
    if (preferences.sound === 'MARIMBA') {
      soundManager.playMarimbaNote();
    } else if (preferences.sound !== 'SILENT') {
      soundManager.playSoftChime();
    }
    setTestSent(true);
    setTimeout(() => setTestSent(false), 4000);
  };

  return (
    <div className="w-full max-w-xl mx-auto px-4 pb-28 pt-4">
      {/* Top Bar */}
      <div className="flex items-center gap-2 mb-4">
        <HavnIconButton
          icon={<ArrowLeft size={18} />}
          contentDescription="Back"
          size={36}
          onClick={onBack}
        />
        <div>
          <span className="text-[10px] font-bold tracking-[0.2em] text-[#8C9287] dark:text-[#73796E] uppercase">
            NOTIFICATIONS & CHIMES
          </span>
          <h1 className="text-xl font-bold text-[#141613] dark:text-[#EDEDEA] font-sans">
            Reminders
          </h1>
        </div>
      </div>

      {/* Test reminder feedback banner */}
      {testSent && (
        <div className="mb-4 p-3 rounded-xl bg-[#E6ECE5] dark:bg-[#222B22] border border-[#516351]/30 dark:border-[#7B947B]/30 flex items-center justify-between animate-fade-in">
          <div className="flex items-center gap-2">
            <Bell size={16} className="text-[#516351] dark:text-[#7B947B]" />
            <div>
              <p className="text-xs font-semibold text-[#141613] dark:text-[#EDEDEA]">
                Test reminder triggered
              </p>
              <p className="text-[11px] text-[#5E645A] dark:text-[#A3A89F]">
                432Hz harmonic chime played
              </p>
            </div>
          </div>
          <span className="text-xs text-[#516351] font-bold">Done</span>
        </div>
      )}

      {/* Unscheduled warning */}
      {unscheduledMeds.length > 0 && (
        <div className="mb-4 p-4 rounded-2xl bg-[#F8E8E2] dark:bg-[#2F1B14] border border-[#9A5637]/30 dark:border-[#D97A52]/30 flex items-start gap-3">
          <AlertCircle size={18} className="text-[#9A5637] dark:text-[#D97A52] shrink-0 mt-0.5" />
          <div>
            <h4 className="text-xs font-bold text-[#9A5637] dark:text-[#D97A52] uppercase tracking-wider">
              {unscheduledMeds.length} Unscheduled Medication{unscheduledMeds.length > 1 ? 's' : ''}
            </h4>
            <p className="text-xs text-[#682D16] dark:text-[#F2B59D] mt-0.5 leading-relaxed">
              {unscheduledMeds.map((m) => m.name).join(', ')} won't chime until you assign a reminder time.
            </p>
          </div>
        </div>
      )}

      <div className="space-y-4">
        {/* Schedule Overview Card */}
        <div className="p-4 rounded-2xl bg-[#FFFFFF] dark:bg-[#1C1F1A] border border-[#E2E1D9] dark:border-[#292E26] shadow-xs">
          <div className="flex items-center justify-between mb-3">
            <h3 className="text-xs font-bold tracking-wider uppercase text-[#141613] dark:text-[#EDEDEA]">
              Active Chimes ({upcomingTimes.length})
            </h3>
            <button
              onClick={handleTestReminder}
              className="text-xs font-medium text-[#516351] dark:text-[#7B947B] hover:underline cursor-pointer"
            >
              Test chime
            </button>
          </div>

          <div className="space-y-2">
            {upcomingTimes.length === 0 ? (
              <p className="text-xs text-[#8C9287] dark:text-[#73796E] py-2">
                No active reminder times.
              </p>
            ) : (
              upcomingTimes.map((time) => {
                const medsAtTime = medications.filter(
                  (m) => m.isActive && m.reminderTimes.includes(time)
                );

                return (
                  <div
                    key={time}
                    className="flex items-center justify-between p-2.5 rounded-xl bg-[#ECEBE4] dark:bg-[#242822] text-xs"
                  >
                    <div className="flex items-center gap-2">
                      <Clock size={14} className="text-[#8C9287]" />
                      <span className="font-bold text-[#141613] dark:text-[#EDEDEA]">
                        {time}
                      </span>
                      <span className="text-[#8C9287] dark:text-[#73796E]">
                        · {medsAtTime.map((m) => m.name).join(', ')}
                      </span>
                    </div>
                  </div>
                );
              })
            )}
          </div>
        </div>

        {/* Reminder Options */}
        <div className="p-4 rounded-2xl bg-[#FFFFFF] dark:bg-[#1C1F1A] border border-[#E2E1D9] dark:border-[#292E26] shadow-xs">
          <h3 className="text-xs font-bold tracking-wider uppercase text-[#141613] dark:text-[#EDEDEA] mb-2">
            Preferences
          </h3>

          <HavnSwitchRow
            title="Pre-dose nudge"
            subtitle="Gentle 15-minute advance notice to have water ready"
            checked={preferences.preDoseEnabled}
            onCheckedChange={(checked) => onUpdatePreferences({ preDoseEnabled: checked })}
          />

          <HavnSwitchRow
            title="Evening check-in"
            subtitle="Recap any untaken doses before bed"
            checked={preferences.eveningCheckEnabled}
            onCheckedChange={(checked) => onUpdatePreferences({ eveningCheckEnabled: checked })}
          />

          {preferences.eveningCheckEnabled && (
            <div className="flex items-center justify-between py-3 border-b border-[#E2E1D9] dark:border-[#292E26] text-xs">
              <span className="text-[#5E645A] dark:text-[#A3A89F] font-medium">Check-in time</span>
              <input
                type="time"
                value={preferences.eveningCheckTime || '20:00'}
                onChange={(e) => onUpdatePreferences({ eveningCheckTime: e.target.value })}
                className="px-2.5 py-1 rounded-lg bg-[#ECEBE4] dark:bg-[#242822] border border-[#E2E1D9] dark:border-[#292E26] font-semibold text-[#141613] dark:text-[#EDEDEA]"
              />
            </div>
          )}

          <HavnSwitchRow
            title="Haptic vibration"
            subtitle="Soft pulse cadence along with the chime"
            checked={preferences.vibration}
            onCheckedChange={(checked) => onUpdatePreferences({ vibration: checked })}
          />
        </div>

        {/* Tone Sound Selector */}
        <div className="p-4 rounded-2xl bg-[#FFFFFF] dark:bg-[#1C1F1A] border border-[#E2E1D9] dark:border-[#292E26] shadow-xs">
          <h3 className="text-xs font-bold tracking-wider uppercase text-[#141613] dark:text-[#EDEDEA] mb-3">
            Tone Style
          </h3>

          <div className="space-y-2">
            {[
              { id: 'CHIME', name: '432Hz Harmonic Chime', desc: 'Resonant bell chime with warm harmonic decay' },
              { id: 'MARIMBA', name: 'Ceramic Wood Marimba', desc: 'Soft organic percussive tones' },
              { id: 'SILENT', name: 'Silent / Visual Only', desc: 'Screen prompts only, no sound playback' },
            ].map((tone) => {
              const isSelected = preferences.sound === tone.id;
              return (
                <div
                  key={tone.id}
                  onClick={() => {
                    soundManager.playSoftTap();
                    onUpdatePreferences({ sound: tone.id as any });
                    if (tone.id === 'CHIME') soundManager.playSoftChime();
                    if (tone.id === 'MARIMBA') soundManager.playMarimbaNote();
                  }}
                  className={`p-3 rounded-xl border flex items-center justify-between cursor-pointer transition-all ${
                    isSelected
                      ? 'bg-[#E6ECE5] dark:bg-[#222B22] border-[#516351] dark:border-[#7B947B]'
                      : 'bg-[#ECEBE4]/50 dark:bg-[#242822]/50 border-transparent hover:bg-[#ECEBE4]'
                  }`}
                >
                  <div>
                    <h4 className="text-xs font-bold text-[#141613] dark:text-[#EDEDEA]">
                      {tone.name}
                    </h4>
                    <p className="text-[11px] text-[#8C9287] dark:text-[#73796E]">
                      {tone.desc}
                    </p>
                  </div>

                  {isSelected && (
                    <div className="w-5 h-5 rounded-full bg-[#516351] dark:bg-[#7B947B] flex items-center justify-center text-white dark:text-[#0E130E]">
                      <Check size={12} strokeWidth={3} />
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
};
