import React, { useState } from 'react';
import { ArrowLeft, Clock, Plus, Trash2, X } from 'lucide-react';
import { Medication, MedIconType, ColorTag, RepeatType } from '../types';
import { COLOR_TAGS, medAccent } from '../theme/tokens';
import { MedIcon } from '../components/MedIcon';
import { HavnTextField } from '../components/HavnTextField';
import { HavnButton, HavnIconButton } from '../components/HavnButton';
import { HavnSegmented } from '../components/HavnSwitch';
import { soundManager } from '../audio/soundManager';

interface AddEditMedicationScreenProps {
  initialMedication?: Medication | null;
  onSave: (medData: Omit<Medication, 'id' | 'createdAt'> & { id?: number }) => void;
  onCancel: () => void;
}

const ICON_TYPES: MedIconType[] = [
  'CAPSULE',
  'TABLET',
  'LIQUID',
  'INJECTION',
  'DROPS',
  'INHALER',
  'PATCH',
];

const REPEAT_OPTIONS: RepeatType[] = ['DAILY', 'WEEKLY', 'AS_NEEDED'];

export const AddEditMedicationScreen: React.FC<AddEditMedicationScreenProps> = ({
  initialMedication,
  onSave,
  onCancel,
}) => {
  const isEditing = !!initialMedication;

  const [name, setName] = useState(initialMedication?.name || '');
  const [dosage, setDosage] = useState(initialMedication?.dosage || '');
  const [colorTag, setColorTag] = useState<ColorTag>(initialMedication?.colorTag || 'sage');
  const [iconType, setIconType] = useState<MedIconType>(initialMedication?.iconType || 'CAPSULE');
  const [repeatType, setRepeatType] = useState<RepeatType>(initialMedication?.repeatType || 'DAILY');
  const [reminderTimes, setReminderTimes] = useState<string[]>(
    initialMedication?.reminderTimes?.length ? initialMedication.reminderTimes : ['08:00']
  );
  const [notes, setNotes] = useState(initialMedication?.notes || '');
  const [newTimeInput, setNewTimeInput] = useState('12:00');
  const [showTimePicker, setShowTimePicker] = useState(false);
  const [nameError, setNameError] = useState<string | null>(null);

  const handleAddReminderTime = () => {
    if (!newTimeInput) return;
    if (!reminderTimes.includes(newTimeInput)) {
      soundManager.playSoftTap();
      setReminderTimes([...reminderTimes, newTimeInput].sort());
    }
    setShowTimePicker(false);
  };

  const handleRemoveReminderTime = (timeToRemove: string) => {
    soundManager.playSoftTap();
    setReminderTimes(reminderTimes.filter((t) => t !== timeToRemove));
  };

  const handleSave = () => {
    if (!name.trim()) {
      setNameError('Please enter a medication name');
      return;
    }

    soundManager.playCeramicClick();
    onSave({
      id: initialMedication?.id,
      userId: initialMedication?.userId || 1,
      name: name.trim(),
      dosage: dosage.trim(),
      reminderTimes,
      repeatType,
      colorTag,
      iconType,
      isActive: initialMedication?.isActive !== undefined ? initialMedication.isActive : true,
      notes: notes.trim(),
    });
  };

  const accent = medAccent(colorTag);

  return (
    <div className="w-full max-w-xl mx-auto px-4 pb-28 pt-4">
      {/* Top Bar */}
      <div className="flex items-center justify-between mb-5">
        <div className="flex items-center gap-2">
          <HavnIconButton
            icon={<ArrowLeft size={18} />}
            contentDescription="Back"
            size={36}
            onClick={onCancel}
          />
          <div>
            <span className="text-[10px] font-bold tracking-[0.2em] text-[#8C9287] dark:text-[#73796E] uppercase">
              {isEditing ? 'EDIT SCHEDULE' : 'NEW ENTRY'}
            </span>
            <h1 className="text-xl font-bold text-[#141613] dark:text-[#EDEDEA] font-sans">
              {isEditing ? 'Edit Medication' : 'Add Medication'}
            </h1>
          </div>
        </div>

        <HavnButton
          text="Save"
          onClick={handleSave}
          size="small"
          fillWidth={false}
          className="px-5"
        />
      </div>

      <div className="space-y-6">
        {/* Basic Info */}
        <div className="p-4 rounded-2xl bg-[#FFFFFF] dark:bg-[#1C1F1A] border border-[#E2E1D9] dark:border-[#292E26] shadow-xs space-y-4">
          <HavnTextField
            label="Medication Name"
            placeholder="e.g. Magnesium glycinate, Vitamin D3"
            value={name}
            onChange={(e) => {
              setName(e.target.value);
              if (nameError) setNameError(null);
            }}
            error={nameError}
          />

          <HavnTextField
            label="Dosage & Form"
            placeholder="e.g. 400 mg, 1 capsule"
            value={dosage}
            onChange={(e) => setDosage(e.target.value)}
          />
        </div>

        {/* Icon & Color Selection */}
        <div className="p-4 rounded-2xl bg-[#FFFFFF] dark:bg-[#1C1F1A] border border-[#E2E1D9] dark:border-[#292E26] shadow-xs space-y-5">
          {/* Form Factor Icons */}
          <div>
            <label className="text-[11px] font-semibold tracking-[0.15em] text-[#8C9287] dark:text-[#73796E] uppercase block mb-2.5">
              Form Factor
            </label>
            <div className="grid grid-cols-4 sm:grid-cols-7 gap-2">
              {ICON_TYPES.map((type) => {
                const isSelected = iconType === type;
                return (
                  <button
                    key={type}
                    type="button"
                    onClick={() => {
                      soundManager.playSoftTap();
                      setIconType(type);
                    }}
                    className={`py-3 rounded-xl flex flex-col items-center justify-center border transition-all cursor-pointer ${
                      isSelected
                        ? 'bg-[#516351] dark:bg-[#7B947B] text-white dark:text-[#0E130E] border-[#516351] dark:border-[#7B947B] shadow-xs'
                        : 'bg-[#ECEBE4]/60 dark:bg-[#242822]/60 text-[#5E645A] dark:text-[#A3A89F] border-transparent hover:bg-[#ECEBE4]'
                    }`}
                  >
                    <MedIcon type={type} size={20} />
                    <span className="text-[9px] font-bold uppercase mt-1 tracking-wider">
                      {type.toLowerCase()}
                    </span>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Color Tag Picker */}
          <div>
            <label className="text-[11px] font-semibold tracking-[0.15em] text-[#8C9287] dark:text-[#73796E] uppercase block mb-2.5">
              Color Tag
            </label>
            <div className="flex items-center gap-3">
              {COLOR_TAGS.map((tag) => {
                const isSelected = colorTag === tag;
                const tagColor = medAccent(tag);
                return (
                  <button
                    key={tag}
                    type="button"
                    onClick={() => {
                      soundManager.playSoftTap();
                      setColorTag(tag);
                    }}
                    style={{ backgroundColor: tagColor }}
                    className={`w-9 h-9 rounded-full transition-transform cursor-pointer relative flex items-center justify-center ${
                      isSelected ? 'scale-110 ring-4 ring-[#516351]/30 dark:ring-[#7B947B]/30' : 'hover:scale-105'
                    }`}
                  >
                    {isSelected && <span className="w-2 h-2 rounded-full bg-white" />}
                  </button>
                );
              })}
            </div>
          </div>
        </div>

        {/* Schedule & Reminder Times */}
        <div className="p-4 rounded-2xl bg-[#FFFFFF] dark:bg-[#1C1F1A] border border-[#E2E1D9] dark:border-[#292E26] shadow-xs space-y-4">
          <div>
            <label className="text-[11px] font-semibold tracking-[0.15em] text-[#8C9287] dark:text-[#73796E] uppercase block mb-2">
              Frequency
            </label>
            <HavnSegmented
              options={['Daily', 'Weekly', 'As Needed']}
              selectedIndex={REPEAT_OPTIONS.indexOf(repeatType)}
              onSelect={(idx) => setRepeatType(REPEAT_OPTIONS[idx])}
            />
          </div>

          {repeatType !== 'AS_NEEDED' && (
            <div>
              <div className="flex items-center justify-between mb-2">
                <label className="text-[11px] font-semibold tracking-[0.15em] text-[#8C9287] dark:text-[#73796E] uppercase">
                  Reminder Times
                </label>
                <button
                  type="button"
                  onClick={() => setShowTimePicker(true)}
                  className="text-xs font-semibold text-[#516351] dark:text-[#7B947B] flex items-center gap-1 hover:underline cursor-pointer"
                >
                  <Plus size={13} />
                  <span>Add time</span>
                </button>
              </div>

              {/* Reminder Pills */}
              <div className="flex flex-wrap gap-2">
                {reminderTimes.map((time) => (
                  <div
                    key={time}
                    className="flex items-center gap-2 px-3 py-1.5 rounded-xl bg-[#ECEBE4] dark:bg-[#242822] text-xs font-semibold text-[#141613] dark:text-[#EDEDEA] border border-[#E2E1D9] dark:border-[#292E26]"
                  >
                    <Clock size={13} className="text-[#8C9287]" />
                    <span>{time}</span>
                    <button
                      type="button"
                      onClick={() => handleRemoveReminderTime(time)}
                      className="text-[#8C9287] hover:text-[#9A5637] cursor-pointer"
                    >
                      <X size={13} />
                    </button>
                  </div>
                ))}

                {reminderTimes.length === 0 && (
                  <p className="text-xs text-[#8C9287] italic">No reminder times set.</p>
                )}
              </div>

              {/* Time Picker Popover */}
              {showTimePicker && (
                <div className="mt-3 p-3 rounded-xl bg-[#ECEBE4] dark:bg-[#242822] border border-[#E2E1D9] dark:border-[#292E26] flex items-center gap-2">
                  <input
                    type="time"
                    value={newTimeInput}
                    onChange={(e) => setNewTimeInput(e.target.value)}
                    className="px-3 py-1.5 text-sm rounded-lg bg-white dark:bg-[#1C1F1A] text-[#141613] dark:text-[#EDEDEA] border border-[#E2E1D9] dark:border-[#292E26]"
                  />
                  <HavnButton
                    text="Add"
                    size="small"
                    fillWidth={false}
                    onClick={handleAddReminderTime}
                  />
                  <HavnButton
                    text="Cancel"
                    tone="ghost"
                    size="small"
                    fillWidth={false}
                    onClick={() => setShowTimePicker(false)}
                  />
                </div>
              )}
            </div>
          )}
        </div>

        {/* Notes */}
        <div className="p-4 rounded-2xl bg-[#FFFFFF] dark:bg-[#1C1F1A] border border-[#E2E1D9] dark:border-[#292E26] shadow-xs">
          <HavnTextField
            label="Instructions & Notes"
            placeholder="e.g. Take 30 minutes before bed with a full glass of water"
            singleLine={false}
            minHeight={80}
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
          />
        </div>

        {/* Action Buttons */}
        <div className="flex items-center gap-3 pt-2">
          <HavnButton text="Cancel" tone="ghost" onClick={onCancel} className="flex-1" />
          <HavnButton text="Save Medication" tone="primary" onClick={handleSave} className="flex-1" />
        </div>
      </div>
    </div>
  );
};
