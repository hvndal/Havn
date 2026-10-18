import React, { useState, useRef } from 'react';
import {
  User as UserIcon,
  Moon,
  Sun,
  Laptop,
  Volume2,
  VolumeX,
  Download,
  Upload,
  Database,
  Trash2,
  Check,
  Plus,
  Info,
  ChevronRight,
  ShieldCheck,
} from 'lucide-react';
import { User, UserPreferences, HavnBackup } from '../types';
import { AVATAR_COLORS } from '../theme/tokens';
import { HavnSwitchRow } from '../components/HavnSwitch';
import { HavnButton } from '../components/HavnButton';
import { HavnConfirmDialog } from '../components/HavnConfirmDialog';
import { HavnBrandLogo } from '../components/HavnBrandLogo';
import { soundManager } from '../audio/soundManager';

interface SettingsScreenProps {
  users: User[];
  activeUser: User | null;
  preferences: UserPreferences;
  onSelectUser: (id: number) => void;
  onCreateUser: (name: string, age?: number, avatarColor?: string) => void;
  onDeleteUser: (id: number) => void;
  onUpdatePreferences: (prefs: Partial<UserPreferences>) => void;
  onExportBackup: () => void;
  onImportBackup: (backup: HavnBackup) => void;
  onSeedSampleHistory: () => void;
  onClearHistory: () => void;
  onOpenManageMeds: () => void;
  onOpenReminders: () => void;
}

export const SettingsScreen: React.FC<SettingsScreenProps> = ({
  users,
  activeUser,
  preferences,
  onSelectUser,
  onCreateUser,
  onDeleteUser,
  onUpdatePreferences,
  onExportBackup,
  onImportBackup,
  onSeedSampleHistory,
  onClearHistory,
  onOpenManageMeds,
  onOpenReminders,
}) => {
  const [showNewUserModal, setShowNewUserModal] = useState(false);
  const [newUserName, setNewUserName] = useState('');
  const [newUserAge, setNewUserAge] = useState('');
  const [newUserColor, setNewUserColor] = useState('#516351');
  const [confirmClearHistory, setConfirmClearHistory] = useState(false);
  const [confirmDeleteUser, setConfirmDeleteUser] = useState<User | null>(null);
  const [importNotice, setImportNotice] = useState<string | null>(null);

  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleCreateUser = () => {
    if (!newUserName.trim()) return;
    soundManager.playCeramicClick();
    onCreateUser(newUserName.trim(), parseInt(newUserAge, 10) || 0, newUserColor);
    setNewUserName('');
    setNewUserAge('');
    setShowNewUserModal(false);
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (event) => {
      try {
        const parsed = JSON.parse(event.target?.result as string);
        onImportBackup(parsed);
        soundManager.playSoftChime();
        setImportNotice('Backup restored successfully');
        setTimeout(() => setImportNotice(null), 4000);
      } catch (err: any) {
        alert('Failed to import backup: ' + (err.message || 'Invalid JSON file'));
      }
    };
    reader.readAsText(file);
    e.target.value = '';
  };

  return (
    <div className="w-full max-w-xl mx-auto px-4 pb-28 pt-4">
      {/* Header */}
      <div className="mb-6">
        <span className="text-[11px] font-semibold tracking-[0.2em] text-[#8C9287] dark:text-[#73796E] uppercase">
          PREFERENCES & SYSTEM
        </span>
        <h1 className="text-2xl font-bold text-[#141613] dark:text-[#EDEDEA] mt-1 font-sans">
          Settings
        </h1>
      </div>

      {importNotice && (
        <div className="mb-4 p-3 rounded-xl bg-[#E6ECE5] dark:bg-[#222B22] border border-[#516351]/30 dark:border-[#7B947B]/30 flex items-center gap-2 text-xs font-semibold text-[#516351] dark:text-[#7B947B]">
          <Check size={16} />
          <span>{importNotice}</span>
        </div>
      )}

      <div className="space-y-4">
        {/* Profile Card */}
        <div className="p-4 rounded-2xl bg-[#FFFFFF] dark:bg-[#1C1F1A] border border-[#E2E1D9] dark:border-[#292E26] shadow-xs">
          <div className="flex items-center justify-between mb-3">
            <h3 className="text-xs font-bold tracking-wider uppercase text-[#141613] dark:text-[#EDEDEA]">
              Profiles ({users.length})
            </h3>
            <button
              onClick={() => setShowNewUserModal(true)}
              className="text-xs font-semibold text-[#516351] dark:text-[#7B947B] flex items-center gap-1 hover:underline cursor-pointer"
            >
              <Plus size={13} />
              <span>New Profile</span>
            </button>
          </div>

          <div className="space-y-2">
            {users.map((u) => {
              const isActive = activeUser?.id === u.id;
              return (
                <div
                  key={u.id}
                  onClick={() => {
                    if (!isActive) {
                      soundManager.playCeramicClick();
                      onSelectUser(u.id);
                    }
                  }}
                  className={`p-3 rounded-xl border flex items-center justify-between transition-all cursor-pointer ${
                    isActive
                      ? 'bg-[#E6ECE5] dark:bg-[#222B22] border-[#516351] dark:border-[#7B947B]'
                      : 'bg-[#ECEBE4]/50 dark:bg-[#242822]/50 border-transparent hover:bg-[#ECEBE4]'
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <div
                      style={{ backgroundColor: u.avatarColor }}
                      className="w-8 h-8 rounded-full flex items-center justify-center text-white text-xs font-bold shadow-xs"
                    >
                      {u.name[0]?.toUpperCase()}
                    </div>
                    <div>
                      <h4 className="text-xs font-bold text-[#141613] dark:text-[#EDEDEA]">
                        {u.name}
                      </h4>
                      <p className="text-[11px] text-[#8C9287] dark:text-[#73796E]">
                        {u.age ? `${u.age} yrs · ` : ''}
                        {isActive ? 'Active profile' : 'Tap to switch'}
                      </p>
                    </div>
                  </div>

                  <div className="flex items-center gap-2">
                    {isActive && (
                      <span className="w-5 h-5 rounded-full bg-[#516351] dark:bg-[#7B947B] flex items-center justify-center text-white text-[10px]">
                        <Check size={12} strokeWidth={3} />
                      </span>
                    )}
                    {users.length > 1 && (
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          setConfirmDeleteUser(u);
                        }}
                        className="p-1 text-[#8C9287] hover:text-[#9A5637] cursor-pointer"
                        title="Delete Profile"
                      >
                        <Trash2 size={14} />
                      </button>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Shortcuts */}
        <div className="p-4 rounded-2xl bg-[#FFFFFF] dark:bg-[#1C1F1A] border border-[#E2E1D9] dark:border-[#292E26] shadow-xs">
          <h3 className="text-xs font-bold tracking-wider uppercase text-[#141613] dark:text-[#EDEDEA] mb-2">
            Medication & Schedule
          </h3>

          <div
            onClick={onOpenManageMeds}
            className="flex items-center justify-between py-3 border-b border-[#E2E1D9] dark:border-[#292E26] cursor-pointer hover:opacity-80"
          >
            <div>
              <h4 className="text-sm font-medium text-[#141613] dark:text-[#EDEDEA]">
                Manage Medications
              </h4>
              <p className="text-xs text-[#8C9287] dark:text-[#73796E]">
                Add, edit, pause, or remove items from your shelf
              </p>
            </div>
            <ChevronRight size={18} className="text-[#8C9287]" />
          </div>

          <div
            onClick={onOpenReminders}
            className="flex items-center justify-between py-3 cursor-pointer hover:opacity-80"
          >
            <div>
              <h4 className="text-sm font-medium text-[#141613] dark:text-[#EDEDEA]">
                Reminders & Chimes
              </h4>
              <p className="text-xs text-[#8C9287] dark:text-[#73796E]">
                Configure alerts, pre-dose nudges, and evening check-ins
              </p>
            </div>
            <ChevronRight size={18} className="text-[#8C9287]" />
          </div>
        </div>

        {/* Appearance & Sound */}
        <div className="p-4 rounded-2xl bg-[#FFFFFF] dark:bg-[#1C1F1A] border border-[#E2E1D9] dark:border-[#292E26] shadow-xs">
          <h3 className="text-xs font-bold tracking-wider uppercase text-[#141613] dark:text-[#EDEDEA] mb-3">
            Appearance & Audio
          </h3>

          {/* Theme Selector */}
          <div className="mb-4">
            <label className="text-xs font-medium text-[#5E645A] dark:text-[#A3A89F] block mb-2">
              Theme Mode
            </label>
            <div className="grid grid-cols-3 gap-2">
              {[
                { id: 'light', label: 'Light', icon: Sun },
                { id: 'dark', label: 'Dark', icon: Moon },
                { id: 'system', label: 'System', icon: Laptop },
              ].map(({ id, label, icon: Icon }) => {
                const isSelected = preferences.theme === id;
                return (
                  <button
                    key={id}
                    onClick={() => {
                      soundManager.playSoftTap();
                      onUpdatePreferences({ theme: id as any });
                    }}
                    className={`py-2 px-3 rounded-xl border flex items-center justify-center gap-1.5 text-xs font-semibold transition-all cursor-pointer ${
                      isSelected
                        ? 'bg-[#516351] dark:bg-[#7B947B] text-white dark:text-[#0E130E] border-[#516351] dark:border-[#7B947B] shadow-xs'
                        : 'bg-[#ECEBE4] dark:bg-[#242822] text-[#5E645A] dark:text-[#A3A89F] border-transparent hover:bg-[#E2E1D9]'
                    }`}
                  >
                    <Icon size={14} />
                    <span>{label}</span>
                  </button>
                );
              })}
            </div>
          </div>

          <HavnSwitchRow
            title="Interface Audio"
            subtitle="Ceramic clicks on lid toggles and 432Hz harmonic chimes"
            checked={preferences.interfaceSound}
            onCheckedChange={(checked) => {
              soundManager.setEnabled(checked);
              onUpdatePreferences({ interfaceSound: checked });
            }}
          />
        </div>

        {/* Data & Backup */}
        <div className="p-4 rounded-2xl bg-[#FFFFFF] dark:bg-[#1C1F1A] border border-[#E2E1D9] dark:border-[#292E26] shadow-xs space-y-3">
          <h3 className="text-xs font-bold tracking-wider uppercase text-[#141613] dark:text-[#EDEDEA]">
            Backup & Data Portability
          </h3>

          <div className="grid grid-cols-2 gap-2 pt-1">
            <HavnButton
              text="Export Backup"
              tone="secondary"
              size="medium"
              onClick={onExportBackup}
              className="gap-1.5"
            >
              <Download size={15} />
              <span>Export</span>
            </HavnButton>

            <HavnButton
              text="Import Backup"
              tone="secondary"
              size="medium"
              onClick={() => fileInputRef.current?.click()}
              className="gap-1.5"
            >
              <Upload size={15} />
              <span>Import</span>
            </HavnButton>
            <input
              ref={fileInputRef}
              type="file"
              accept=".json"
              onChange={handleFileChange}
              className="hidden"
            />
          </div>

          <div className="pt-2 border-t border-[#E2E1D9] dark:border-[#292E26] space-y-2">
            <button
              onClick={() => {
                soundManager.playSoftTap();
                onSeedSampleHistory();
              }}
              className="w-full text-left py-2 px-3 text-xs font-medium text-[#516351] dark:text-[#7B947B] hover:bg-[#ECEBE4] dark:hover:bg-[#242822] rounded-lg transition-colors flex items-center justify-between cursor-pointer"
            >
              <span>Seed 30-day realistic sample history</span>
              <Database size={14} />
            </button>

            <button
              onClick={() => setConfirmClearHistory(true)}
              className="w-full text-left py-2 px-3 text-xs font-medium text-[#9A5637] dark:text-[#D97A52] hover:bg-[#F8E8E2] dark:hover:bg-[#2F1B14] rounded-lg transition-colors flex items-center justify-between cursor-pointer"
            >
              <span>Clear dose log history</span>
              <Trash2 size={14} />
            </button>
          </div>
        </div>

        {/* Brand & Mission Statement */}
        <div className="p-6 rounded-2xl bg-[#FFFFFF] dark:bg-[#1C1F1A] border border-[#E2E1D9] dark:border-[#292E26] shadow-xs text-center flex flex-col items-center">
          <HavnBrandLogo iconSize={40} showWordmark={true} showTagline={true} />
          <p className="text-xs text-[#8C9287] dark:text-[#73796E] mt-3 max-w-xs leading-relaxed">
            Hävn is designed as a peaceful, offline-first, private sanctuary for your daily health routine. No telemetry, no ads.
          </p>
          <div className="mt-4 flex items-center gap-1.5 text-[11px] text-[#516351] dark:text-[#7B947B] font-semibold">
            <ShieldCheck size={14} />
            <span>Local & Private Storage · Version 1.0.1</span>
          </div>
        </div>
      </div>

      {/* New User Modal */}
      {showNewUserModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-xs">
          <div className="w-full max-w-sm p-6 bg-[#FFFFFF] dark:bg-[#1C1F1A] rounded-2xl shadow-xl border border-[#E2E1D9] dark:border-[#292E26]">
            <h3 className="text-lg font-bold text-[#141613] dark:text-[#EDEDEA] mb-4">
              Add New Profile
            </h3>

            <div className="space-y-4">
              <div>
                <label className="text-xs font-medium text-[#8C9287] block mb-1">Name</label>
                <input
                  value={newUserName}
                  onChange={(e) => setNewUserName(e.target.value)}
                  placeholder="e.g. Maya, David"
                  className="w-full px-3 py-2 text-sm rounded-xl bg-[#ECEBE4] dark:bg-[#242822] text-[#141613] dark:text-[#EDEDEA] border border-transparent focus:border-[#516351] focus:outline-hidden"
                />
              </div>

              <div>
                <label className="text-xs font-medium text-[#8C9287] block mb-1">Age (optional)</label>
                <input
                  type="number"
                  value={newUserAge}
                  onChange={(e) => setNewUserAge(e.target.value)}
                  placeholder="e.g. 32"
                  className="w-full px-3 py-2 text-sm rounded-xl bg-[#ECEBE4] dark:bg-[#242822] text-[#141613] dark:text-[#EDEDEA] border border-transparent focus:border-[#516351] focus:outline-hidden"
                />
              </div>

              <div>
                <label className="text-xs font-medium text-[#8C9287] block mb-2">Avatar Color</label>
                <div className="flex items-center gap-3">
                  {AVATAR_COLORS.map((c) => (
                    <button
                      key={c}
                      type="button"
                      onClick={() => setNewUserColor(c)}
                      style={{ backgroundColor: c }}
                      className={`w-7 h-7 rounded-full transition-transform cursor-pointer ${
                        newUserColor === c ? 'scale-125 ring-2 ring-[#516351]' : ''
                      }`}
                    />
                  ))}
                </div>
              </div>

              <div className="mt-6 flex items-center gap-3">
                <HavnButton
                  text="Cancel"
                  tone="ghost"
                  size="medium"
                  onClick={() => setShowNewUserModal(false)}
                  className="flex-1"
                />
                <HavnButton
                  text="Create"
                  tone="primary"
                  size="medium"
                  onClick={handleCreateUser}
                  className="flex-1"
                />
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Clear History Confirmation */}
      <HavnConfirmDialog
        isOpen={confirmClearHistory}
        title="Clear Dose History?"
        body="This will erase recorded doses and streak logs. Your medication list and reminders will remain intact."
        confirmLabel="Clear History"
        destructive
        onConfirm={() => {
          soundManager.playCeramicClick();
          onClearHistory();
          setConfirmClearHistory(false);
        }}
        onDismiss={() => setConfirmClearHistory(false)}
      />

      {/* Delete User Confirmation */}
      <HavnConfirmDialog
        isOpen={!!confirmDeleteUser}
        title={`Delete profile ${confirmDeleteUser?.name}?`}
        body="All medications, dose logs, and settings associated with this profile will be permanently removed."
        confirmLabel="Delete Profile"
        destructive
        onConfirm={() => {
          if (confirmDeleteUser) {
            soundManager.playCeramicClick();
            onDeleteUser(confirmDeleteUser.id);
            setConfirmDeleteUser(null);
          }
        }}
        onDismiss={() => setConfirmDeleteUser(null)}
      />
    </div>
  );
};
