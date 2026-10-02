import React, { useState, useMemo } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { Plus, Search, Trash2, Edit2, ArrowLeft, Clock, X } from 'lucide-react';
import { Medication } from '../types';
import { MedIcon } from '../components/MedIcon';
import { medAccent } from '../theme/tokens';
import { HavnSwitch } from '../components/HavnSwitch';
import { HavnButton, HavnIconButton } from '../components/HavnButton';
import { HavnConfirmDialog } from '../components/HavnConfirmDialog';
import { soundManager } from '../audio/soundManager';

interface ManageMedicationsScreenProps {
  medications: Medication[];
  onAddMedication: () => void;
  onEditMedication: (med: Medication) => void;
  onToggleActive: (id: number) => void;
  onDeleteMedication: (id: number) => void;
  onBack: () => void;
}

export const ManageMedicationsScreen: React.FC<ManageMedicationsScreenProps> = ({
  medications,
  onAddMedication,
  onEditMedication,
  onToggleActive,
  onDeleteMedication,
  onBack,
}) => {
  const [search, setSearch] = useState('');
  const [tab, setTab] = useState<'ALL' | 'ACTIVE' | 'PAUSED'>('ALL');
  const [medToDelete, setMedToDelete] = useState<Medication | null>(null);

  const filteredMeds = useMemo(() => {
    return medications.filter((m) => {
      const matchesSearch =
        m.name.toLowerCase().includes(search.toLowerCase()) ||
        m.dosage.toLowerCase().includes(search.toLowerCase()) ||
        m.notes.toLowerCase().includes(search.toLowerCase());

      if (!matchesSearch) return false;
      if (tab === 'ACTIVE') return m.isActive;
      if (tab === 'PAUSED') return !m.isActive;
      return true;
    });
  }, [medications, search, tab]);

  const handleDeleteConfirm = () => {
    if (medToDelete) {
      soundManager.playCeramicClick();
      onDeleteMedication(medToDelete.id);
      setMedToDelete(null);
    }
  };

  return (
    <div className="w-full max-w-xl mx-auto px-4 pb-28 pt-4">
      {/* Top Bar */}
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-2">
          <HavnIconButton
            icon={<ArrowLeft size={18} />}
            contentDescription="Back"
            size={36}
            onClick={onBack}
          />
          <div>
            <span className="text-[10px] font-bold tracking-[0.2em] text-[#8C9287] dark:text-[#73796E] uppercase">
              SHELF & SCHEDULE
            </span>
            <h1 className="text-xl font-bold text-[#141613] dark:text-[#EDEDEA] font-sans">
              Medications
            </h1>
          </div>
        </div>

        <HavnButton
          text="Add"
          onClick={onAddMedication}
          size="small"
          fillWidth={false}
          className="gap-1"
        >
          <Plus size={14} />
          <span>Add</span>
        </HavnButton>
      </div>

      {/* Search Input */}
      <div className="relative mb-3">
        <label htmlFor="medication-search-input" className="sr-only">
          Search medications by name
        </label>
        <Search
          size={16}
          className="absolute left-3.5 top-1/2 -translate-y-1/2 text-[#8C9287] dark:text-[#73796E] pointer-events-none"
        />
        <input
          id="medication-search-input"
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Escape') {
              setSearch('');
              soundManager.playSoftTap();
            }
          }}
          placeholder="Search medications by name or notes..."
          className="w-full pl-10 pr-10 py-2.5 text-xs rounded-xl bg-[#ECEBE4] dark:bg-[#1C1F1A] text-[#141613] dark:text-[#EDEDEA] placeholder-[#8C9287]/70 dark:placeholder-[#73796E]/70 border border-[#E2E1D9]/60 dark:border-[#292E26]/60 focus:border-[#516351] dark:focus:border-[#7B947B] focus:outline-hidden transition-colors"
        />
        {search && (
          <button
            type="button"
            onClick={() => {
              setSearch('');
              soundManager.playSoftTap();
            }}
            aria-label="Clear search"
            className="absolute right-3 top-1/2 -translate-y-1/2 p-1 rounded-full text-[#8C9287] hover:text-[#141613] dark:hover:text-[#EDEDEA] hover:bg-[#E2E1D9] dark:hover:bg-[#292E26] transition-colors cursor-pointer"
          >
            <X size={14} />
          </button>
        )}
      </div>

      {/* Search match indicator */}
      {search.trim() && (
        <div className="flex items-center justify-between px-1 mb-2.5 text-[11px] text-[#5E645A] dark:text-[#A3A89F]">
          <span>
            {filteredMeds.length} {filteredMeds.length === 1 ? 'match' : 'matches'} for "{search}"
          </span>
          <button
            type="button"
            onClick={() => {
              setSearch('');
              soundManager.playSoftTap();
            }}
            className="text-[#516351] dark:text-[#7B947B] font-semibold hover:underline cursor-pointer"
          >
            Reset
          </button>
        </div>
      )}

      {/* Filter Tabs */}
      <div className="flex items-center gap-1 mb-4 bg-[#ECEBE4] dark:bg-[#1C1F1A] p-1 rounded-xl border border-[#E2E1D9] dark:border-[#292E26]">
        {(['ALL', 'ACTIVE', 'PAUSED'] as const).map((t) => {
          const isSelected = tab === t;
          const count =
            t === 'ALL'
              ? medications.length
              : t === 'ACTIVE'
              ? medications.filter((m) => m.isActive).length
              : medications.filter((m) => !m.isActive).length;

          return (
            <button
              key={t}
              onClick={() => {
                soundManager.playSoftTap();
                setTab(t);
              }}
              className={`flex-1 py-1.5 text-xs font-semibold rounded-lg transition-all cursor-pointer ${
                isSelected
                  ? 'bg-[#FFFFFF] dark:bg-[#282E25] text-[#141613] dark:text-[#EDEDEA] shadow-2xs'
                  : 'text-[#8C9287] dark:text-[#73796E] hover:text-[#141613] dark:hover:text-[#EDEDEA]'
              }`}
            >
              {t} ({count})
            </button>
          );
        })}
      </div>

      {/* Medication List */}
      <div className="space-y-3">
        <AnimatePresence mode="popLayout">
          {filteredMeds.length === 0 ? (
            <div className="py-12 text-center rounded-2xl bg-[#FFFFFF] dark:bg-[#1C1F1A] border border-[#E2E1D9] dark:border-[#292E26]">
              <p className="text-sm font-semibold text-[#141613] dark:text-[#EDEDEA]">
                No medications found
              </p>
              <p className="text-xs text-[#8C9287] dark:text-[#73796E] mt-1">
                {search ? `No medications match "${search}"` : 'Your shelf is currently empty.'}
              </p>
              {search && (
                <button
                  type="button"
                  onClick={() => {
                    setSearch('');
                    soundManager.playSoftTap();
                  }}
                  className="mt-3 px-3 py-1.5 rounded-lg text-xs font-semibold bg-[#ECEBE4] dark:bg-[#242822] text-[#516351] dark:text-[#7B947B] hover:bg-[#E2E1D9] dark:hover:bg-[#292E26] transition-colors cursor-pointer"
                >
                  Clear search
                </button>
              )}
            </div>
          ) : (
            filteredMeds.map((med) => {
              const accent = medAccent(med.colorTag);

              return (
                <motion.div
                  key={med.id}
                  layout
                  initial={{ opacity: 0, y: 6 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, scale: 0.95 }}
                  className={`p-4 rounded-2xl bg-[#FFFFFF] dark:bg-[#1C1F1A] border border-[#E2E1D9] dark:border-[#292E26] shadow-xs transition-opacity ${
                    !med.isActive ? 'opacity-60' : 'opacity-100'
                  }`}
                >
                  <div className="flex items-start justify-between gap-3">
                    <div className="flex items-start gap-3 flex-1 min-w-0">
                      <div
                        style={{ backgroundColor: `${accent}20` }}
                        className="w-11 h-11 rounded-xl flex items-center justify-center shrink-0 border border-transparent"
                      >
                        <MedIcon type={med.iconType} size={22} color={accent} />
                      </div>

                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2">
                          <h3 className="text-sm font-bold text-[#141613] dark:text-[#EDEDEA] truncate">
                            {med.name}
                          </h3>
                          {!med.isActive && (
                            <span className="text-[10px] font-semibold px-2 py-0.5 rounded bg-[#ECEBE4] dark:bg-[#282E25] text-[#8C9287] dark:text-[#73796E]">
                              Paused
                            </span>
                          )}
                        </div>

                        <p className="text-xs text-[#5E645A] dark:text-[#A3A89F] mt-0.5">
                          {med.dosage}
                        </p>

                        {med.reminderTimes.length > 0 && (
                          <div className="flex items-center gap-1.5 mt-2 text-[11px] text-[#8C9287] dark:text-[#73796E]">
                            <Clock size={12} />
                            <span>{med.reminderTimes.join(', ')}</span>
                            <span>· {med.repeatType.toLowerCase()}</span>
                          </div>
                        )}

                        {med.notes && (
                          <p className="text-[11px] text-[#8C9287] dark:text-[#73796E] mt-1.5 italic">
                            "{med.notes}"
                          </p>
                        )}
                      </div>
                    </div>

                    <div className="flex flex-col items-end gap-3 shrink-0">
                      <HavnSwitch
                        checked={med.isActive}
                        onCheckedChange={() => onToggleActive(med.id)}
                        contentDescription={`Toggle ${med.name} active`}
                      />

                      <div className="flex items-center gap-1">
                        <button
                          onClick={() => onEditMedication(med)}
                          className="p-1.5 rounded-lg text-[#8C9287] hover:text-[#141613] dark:hover:text-[#EDEDEA] hover:bg-[#ECEBE4] dark:hover:bg-[#242822] cursor-pointer"
                          title="Edit"
                        >
                          <Edit2 size={15} />
                        </button>
                        <button
                          onClick={() => setMedToDelete(med)}
                          className="p-1.5 rounded-lg text-[#8C9287] hover:text-[#9A5637] dark:hover:text-[#D97A52] hover:bg-[#ECEBE4] dark:hover:bg-[#242822] cursor-pointer"
                          title="Delete"
                        >
                          <Trash2 size={15} />
                        </button>
                      </div>
                    </div>
                  </div>
                </motion.div>
              );
            })
          )}
        </AnimatePresence>
      </div>

      {/* Delete Confirmation Modal */}
      <HavnConfirmDialog
        isOpen={!!medToDelete}
        title={`Delete ${medToDelete?.name}?`}
        body="This will remove the medication and all of its scheduled reminders from your shelf. Past dose history remains intact."
        confirmLabel="Delete"
        destructive
        onConfirm={handleDeleteConfirm}
        onDismiss={() => setMedToDelete(null)}
      />
    </div>
  );
};
