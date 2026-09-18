import React, { useState, useEffect, useMemo, useCallback } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import {
  CalendarDays,
  Box,
  TrendingUp,
  Settings as SettingsIcon,
  Plus,
} from 'lucide-react';
import { User, Medication, DoseLog, DoseStatus, UserPreferences, HavnBackup } from './types';
import { storageService } from './data/storage';
import { soundManager } from './audio/soundManager';
import { HavnAmbientField } from './components/HavnAmbientField';

// Screens
import { HomeScreen } from './screens/HomeScreen';
import { OrganizerScreen } from './screens/OrganizerScreen';
import { ProgressScreen } from './screens/ProgressScreen';
import { SettingsScreen } from './screens/SettingsScreen';
import { ManageMedicationsScreen } from './screens/ManageMedicationsScreen';
import { AddEditMedicationScreen } from './screens/AddEditMedicationScreen';
import { RemindersScreen } from './screens/RemindersScreen';
import { OnboardingScreen } from './screens/OnboardingScreen';

type MainTab = 'TODAY' | 'ORGANIZER' | 'PROGRESS' | 'SETTINGS';
type OverlayView = 'NONE' | 'MANAGE_MEDS' | 'ADD_MED' | 'EDIT_MED' | 'REMINDERS';

export function App() {
  const [users, setUsers] = useState<User[]>(() => storageService.getUsers());
  const [activeUserId, setActiveUserId] = useState<number>(() => storageService.getActiveUserId());
  const [preferences, setPreferences] = useState<UserPreferences>(() => storageService.getPreferences());
  const [medications, setMedications] = useState<Medication[]>(() => storageService.getMedications(activeUserId));
  const [doseLogs, setDoseLogs] = useState<DoseLog[]>(() => storageService.getDoseLogs(activeUserId));

  const [currentTab, setCurrentTab] = useState<MainTab>('TODAY');
  const [overlayView, setOverlayView] = useState<OverlayView>('NONE');
  const [editingMedication, setEditingMedication] = useState<Medication | null>(null);
  const [selectedDate, setSelectedDate] = useState<Date>(() => new Date());

  const activeUser = useMemo(() => {
    return users.find((u) => u.id === activeUserId) || users[0] || null;
  }, [users, activeUserId]);

  // Sync sound manager enabled preference
  useEffect(() => {
    soundManager.setEnabled(preferences.interfaceSound);
  }, [preferences.interfaceSound]);

  // Sync Theme Mode
  const isDark = useMemo(() => {
    if (preferences.theme === 'dark') return true;
    if (preferences.theme === 'light') return false;
    if (typeof window !== 'undefined' && window.matchMedia) {
      return window.matchMedia('(prefers-color-scheme: dark)').matches;
    }
    return false;
  }, [preferences.theme]);

  useEffect(() => {
    if (isDark) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }, [isDark]);

  // Ensure today's dose logs exist on load and date change
  useEffect(() => {
    const updated = storageService.ensureDoseLogsForDate(selectedDate, activeUserId);
    setDoseLogs(storageService.getDoseLogs(activeUserId));
  }, [selectedDate, activeUserId]);

  // Reload user-scoped data
  const reloadData = useCallback(() => {
    const allUsers = storageService.getUsers();
    const currId = storageService.getActiveUserId();
    setUsers(allUsers);
    setActiveUserId(currId);
    setMedications(storageService.getMedications(currId));
    setDoseLogs(storageService.getDoseLogs(currId));
    setPreferences(storageService.getPreferences());
  }, []);

  // Update dose log status
  const handleUpdateDoseStatus = useCallback(
    (logId: number, status: DoseStatus) => {
      storageService.updateDoseLogStatus(logId, status);
      setDoseLogs(storageService.getDoseLogs(activeUserId));
    },
    [activeUserId]
  );

  // User Profile Switching
  const handleSelectUser = (id: number) => {
    storageService.setActiveUserId(id);
    setActiveUserId(id);
    setMedications(storageService.getMedications(id));
    setDoseLogs(storageService.getDoseLogs(id));
  };

  const handleCreateUser = (name: string, age?: number, avatarColor?: string) => {
    const newUser = storageService.createUser(name, age || 0, avatarColor);
    reloadData();
  };

  const handleDeleteUser = (id: number) => {
    storageService.deleteUser(id);
    reloadData();
  };

  // Preference updates
  const handleUpdatePreferences = (prefs: Partial<UserPreferences>) => {
    const updated = storageService.savePreferences(prefs);
    setPreferences(updated);
  };

  // Medication Management
  const handleSaveMedication = (
    medData: Omit<Medication, 'id' | 'createdAt'> & { id?: number }
  ) => {
    storageService.saveMedication({
      ...medData,
      userId: activeUserId,
    });
    setMedications(storageService.getMedications(activeUserId));
    setDoseLogs(storageService.getDoseLogs(activeUserId));
    setOverlayView('NONE');
    setEditingMedication(null);
  };

  const handleToggleMedActive = (id: number) => {
    storageService.toggleMedicationActive(id);
    setMedications(storageService.getMedications(activeUserId));
    setDoseLogs(storageService.getDoseLogs(activeUserId));
  };

  const handleDeleteMedication = (id: number) => {
    storageService.deleteMedication(id);
    setMedications(storageService.getMedications(activeUserId));
    setDoseLogs(storageService.getDoseLogs(activeUserId));
  };

  // Maintenance: Seed sample history
  const handleSeedSampleHistory = () => {
    storageService.seedSampleHistory();
    reloadData();
  };

  // Maintenance: Clear dose history
  const handleClearHistory = () => {
    storageService.clearDoseHistory(activeUserId);
    setDoseLogs(storageService.getDoseLogs(activeUserId));
  };

  // Export backup to JSON file download
  const handleExportBackup = () => {
    const backup = storageService.exportBackup();
    const dataStr = 'data:text/json;charset=utf-8,' + encodeURIComponent(JSON.stringify(backup, null, 2));
    const downloadAnchor = document.createElement('a');
    downloadAnchor.setAttribute('href', dataStr);
    downloadAnchor.setAttribute(
      'download',
      `havn-backup-${new Date().toISOString().slice(0, 10)}.json`
    );
    document.body.appendChild(downloadAnchor);
    downloadAnchor.click();
    downloadAnchor.remove();
    soundManager.playSoftChime();
  };

  // Import backup from JSON
  const handleImportBackup = (backup: HavnBackup) => {
    storageService.restoreBackup(backup);
    reloadData();
  };

  if (users.length === 0) {
    return (
      <OnboardingScreen
        onComplete={(name, age, color) => {
          storageService.createUser(name, age, color);
          reloadData();
        }}
      />
    );
  }

  return (
    <div className="relative min-h-screen w-full bg-[#FBFBFA] dark:bg-[#141613] text-[#141613] dark:text-[#EDEDEA] transition-colors duration-300 flex flex-col font-sans overflow-x-hidden">
      <HavnAmbientField />

      {/* Main Content Area */}
      <main className="flex-1 w-full relative z-10">
        <AnimatePresence mode="wait">
          {overlayView === 'NONE' && (
            <motion.div
              key={currentTab}
              initial={{ opacity: 0, y: 4 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -4 }}
              transition={{ duration: 0.16 }}
              className="w-full"
            >
              {currentTab === 'TODAY' && (
                <HomeScreen
                  user={activeUser}
                  medications={medications}
                  doseLogs={doseLogs}
                  selectedDate={selectedDate}
                  onSelectDate={setSelectedDate}
                  onUpdateDoseStatus={handleUpdateDoseStatus}
                  onAddMedication={() => {
                    setEditingMedication(null);
                    setOverlayView('ADD_MED');
                  }}
                  onOpenManageMeds={() => setOverlayView('MANAGE_MEDS')}
                />
              )}

              {currentTab === 'ORGANIZER' && (
                <OrganizerScreen
                  medications={medications}
                  doseLogs={doseLogs}
                  selectedDate={selectedDate}
                  onSelectDate={setSelectedDate}
                  onUpdateDoseStatus={handleUpdateDoseStatus}
                  isDark={isDark}
                />
              )}

              {currentTab === 'PROGRESS' && (
                <ProgressScreen
                  medications={medications}
                  doseLogs={doseLogs}
                  onSelectDate={setSelectedDate}
                  onNavigateHome={() => setCurrentTab('TODAY')}
                />
              )}

              {currentTab === 'SETTINGS' && (
                <SettingsScreen
                  users={users}
                  activeUser={activeUser}
                  preferences={preferences}
                  onSelectUser={handleSelectUser}
                  onCreateUser={handleCreateUser}
                  onDeleteUser={handleDeleteUser}
                  onUpdatePreferences={handleUpdatePreferences}
                  onExportBackup={handleExportBackup}
                  onImportBackup={handleImportBackup}
                  onSeedSampleHistory={handleSeedSampleHistory}
                  onClearHistory={handleClearHistory}
                  onOpenManageMeds={() => setOverlayView('MANAGE_MEDS')}
                  onOpenReminders={() => setOverlayView('REMINDERS')}
                />
              )}
            </motion.div>
          )}

          {overlayView === 'MANAGE_MEDS' && (
            <motion.div
              key="manage_meds"
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: 20 }}
              className="w-full"
            >
              <ManageMedicationsScreen
                medications={medications}
                onAddMedication={() => {
                  setEditingMedication(null);
                  setOverlayView('ADD_MED');
                }}
                onEditMedication={(med) => {
                  setEditingMedication(med);
                  setOverlayView('EDIT_MED');
                }}
                onToggleActive={handleToggleMedActive}
                onDeleteMedication={handleDeleteMedication}
                onBack={() => setOverlayView('NONE')}
              />
            </motion.div>
          )}

          {(overlayView === 'ADD_MED' || overlayView === 'EDIT_MED') && (
            <motion.div
              key="add_edit_med"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: 20 }}
              className="w-full"
            >
              <AddEditMedicationScreen
                initialMedication={editingMedication}
                onSave={handleSaveMedication}
                onCancel={() => {
                  setEditingMedication(null);
                  setOverlayView('NONE');
                }}
              />
            </motion.div>
          )}

          {overlayView === 'REMINDERS' && (
            <motion.div
              key="reminders"
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: 20 }}
              className="w-full"
            >
              <RemindersScreen
                medications={medications}
                preferences={preferences}
                onUpdatePreferences={handleUpdatePreferences}
                onBack={() => setOverlayView('NONE')}
              />
            </motion.div>
          )}
        </AnimatePresence>
      </main>

      {/* Persistent Bottom Navigation Bar */}
      {overlayView === 'NONE' && (
        <nav className="fixed bottom-0 left-0 right-0 z-40 bg-[#FBFBFA]/90 dark:bg-[#141613]/90 backdrop-blur-md border-t border-[#E2E1D9] dark:border-[#292E26] py-2 px-4 select-none">
          <div className="max-w-md mx-auto flex items-center justify-around">
            {[
              { id: 'TODAY', label: 'Today', icon: CalendarDays },
              { id: 'ORGANIZER', label: 'Organiser', icon: Box },
              { id: 'PROGRESS', label: 'Progress', icon: TrendingUp },
              { id: 'SETTINGS', label: 'Settings', icon: SettingsIcon },
            ].map(({ id, label, icon: Icon }) => {
              const isSelected = currentTab === id;
              return (
                <button
                  key={id}
                  onClick={() => {
                    if (currentTab !== id) {
                      soundManager.playSoftTap();
                      setCurrentTab(id as MainTab);
                    }
                  }}
                  className={`flex flex-col items-center justify-center py-1 px-3 rounded-2xl transition-all cursor-pointer relative ${
                    isSelected
                      ? 'text-[#516351] dark:text-[#7B947B]'
                      : 'text-[#8C9287] dark:text-[#73796E] hover:text-[#141613] dark:hover:text-[#EDEDEA]'
                  }`}
                >
                  <Icon size={20} strokeWidth={isSelected ? 2.3 : 1.8} />
                  <span className="text-[10px] font-semibold mt-1 tracking-wider">
                    {label}
                  </span>
                  {isSelected && (
                    <motion.div
                      layoutId="tabDot"
                      className="absolute -bottom-1 w-1 h-1 rounded-full bg-[#516351] dark:bg-[#7B947B]"
                    />
                  )}
                </button>
              );
            })}
          </div>
        </nav>
      )}
    </div>
  );
}

export default App;
