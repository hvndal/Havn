import {
  User,
  Medication,
  DoseLog,
  DoseStatus,
  UserPreferences,
  HavnBackup,
  BackupProfile,
  BackupMedication,
  BackupDoseLog,
} from '../types';

const STORAGE_KEYS = {
  USERS: 'havn_users',
  ACTIVE_USER_ID: 'havn_active_user_id',
  MEDICATIONS: 'havn_medications',
  DOSE_LOGS: 'havn_dose_logs',
  PREFERENCES: 'havn_preferences',
};

const DEFAULT_PREFERENCES: UserPreferences = {
  theme: 'system',
  interfaceSound: true,
  preDoseEnabled: true,
  eveningCheckEnabled: true,
  eveningCheckTime: '20:00',
  vibration: true,
  sound: 'CHIME',
};

// Initial profiles and medications
const INITIAL_USERS: User[] = [
  {
    id: 1,
    name: 'Herman',
    age: 32,
    avatarColor: '#516351', // sage
    createdAt: Date.now() - 30 * 24 * 3600 * 1000,
  },
  {
    id: 2,
    name: 'Maya',
    age: 29,
    avatarColor: '#9A5637', // clay
    createdAt: Date.now() - 15 * 24 * 3600 * 1000,
  },
];

const INITIAL_MEDICATIONS: Medication[] = [
  {
    id: 1,
    userId: 1,
    name: 'Magnesium glycinate',
    dosage: '400 mg, 1 capsule',
    reminderTimes: ['21:00'],
    repeatType: 'DAILY',
    colorTag: 'sage',
    iconType: 'CAPSULE',
    isActive: true,
    notes: 'Take 30 minutes before bed with water',
    createdAt: Date.now() - 30 * 24 * 3600 * 1000,
  },
  {
    id: 2,
    userId: 1,
    name: 'Vitamin D3 + K2',
    dosage: '2000 IU',
    reminderTimes: ['08:00'],
    repeatType: 'DAILY',
    colorTag: 'amber',
    iconType: 'DROPS',
    isActive: true,
    notes: 'Take in the morning with food (fat-soluble)',
    createdAt: Date.now() - 30 * 24 * 3600 * 1000,
  },
  {
    id: 3,
    userId: 1,
    name: 'Omega-3 Fish Oil',
    dosage: '1000 mg EPA/DHA',
    reminderTimes: ['12:30'],
    repeatType: 'DAILY',
    colorTag: 'clay',
    iconType: 'CAPSULE',
    isActive: true,
    notes: 'Take with lunch',
    createdAt: Date.now() - 25 * 24 * 3600 * 1000,
  },
  {
    id: 4,
    userId: 1,
    name: 'L-Theanine',
    dosage: '200 mg',
    reminderTimes: ['14:00'],
    repeatType: 'DAILY',
    colorTag: 'slate',
    iconType: 'TABLET',
    isActive: true,
    notes: 'Optional afternoon focus support',
    createdAt: Date.now() - 20 * 24 * 3600 * 1000,
  },
];

function generateSeedDoseLogs(users: User[], medications: Medication[]): DoseLog[] {
  const logs: DoseLog[] = [];
  let logId = 1;
  const now = new Date();

  // Create 21 days of history up to today
  for (let dayOffset = 21; dayOffset >= 0; dayOffset--) {
    const targetDate = new Date(now.getTime() - dayOffset * 24 * 3600 * 1000);
    const dateStr = targetDate.toISOString().slice(0, 10);

    for (const med of medications) {
      if (!med.isActive) continue;
      for (const time of med.reminderTimes) {
        const [hours, mins] = time.split(':').map(Number);
        const scheduled = new Date(targetDate);
        scheduled.setHours(hours, mins, 0, 0);
        const scheduledTime = scheduled.getTime();

        const isPast = scheduledTime < now.getTime();
        const isToday = dayOffset === 0;

        let status: DoseStatus = 'PENDING';
        let takenAt: number | null = null;

        if (isPast) {
          // 85% compliance rate for realistic history
          const rand = Math.random();
          if (rand > 0.15) {
            status = 'TAKEN';
            takenAt = scheduledTime + Math.floor(Math.random() * 20 * 60 * 1000); // 0-20 min late
          } else if (rand > 0.05) {
            status = 'SKIPPED';
          } else {
            status = 'SNOOZED';
          }
        } else if (isToday) {
          status = 'PENDING';
        }

        logs.push({
          id: logId++,
          medicationId: med.id,
          userId: med.userId,
          scheduledTime,
          takenAt,
          status,
          scheduledSlot: time,
        });
      }
    }
  }

  return logs;
}

export class StorageService {
  private static instance: StorageService;

  private constructor() {
    this.ensureInitialized();
  }

  static getInstance(): StorageService {
    if (!StorageService.instance) {
      StorageService.instance = new StorageService();
    }
    return StorageService.instance;
  }

  private ensureInitialized() {
    if (typeof window === 'undefined') return;

    if (!localStorage.getItem(STORAGE_KEYS.USERS)) {
      localStorage.setItem(STORAGE_KEYS.USERS, JSON.stringify(INITIAL_USERS));
    }
    if (!localStorage.getItem(STORAGE_KEYS.ACTIVE_USER_ID)) {
      localStorage.setItem(STORAGE_KEYS.ACTIVE_USER_ID, '1');
    }
    if (!localStorage.getItem(STORAGE_KEYS.MEDICATIONS)) {
      localStorage.setItem(STORAGE_KEYS.MEDICATIONS, JSON.stringify(INITIAL_MEDICATIONS));
    }
    if (!localStorage.getItem(STORAGE_KEYS.DOSE_LOGS)) {
      const logs = generateSeedDoseLogs(INITIAL_USERS, INITIAL_MEDICATIONS);
      localStorage.setItem(STORAGE_KEYS.DOSE_LOGS, JSON.stringify(logs));
    }
    if (!localStorage.getItem(STORAGE_KEYS.PREFERENCES)) {
      localStorage.setItem(STORAGE_KEYS.PREFERENCES, JSON.stringify(DEFAULT_PREFERENCES));
    }
  }

  // ── Preferences ────────────────────────────────────────────────────────────
  getPreferences(): UserPreferences {
    try {
      const raw = localStorage.getItem(STORAGE_KEYS.PREFERENCES);
      return raw ? { ...DEFAULT_PREFERENCES, ...JSON.parse(raw) } : DEFAULT_PREFERENCES;
    } catch {
      return DEFAULT_PREFERENCES;
    }
  }

  savePreferences(prefs: Partial<UserPreferences>): UserPreferences {
    const current = this.getPreferences();
    const updated = { ...current, ...prefs };
    localStorage.setItem(STORAGE_KEYS.PREFERENCES, JSON.stringify(updated));
    return updated;
  }

  // ── Users ──────────────────────────────────────────────────────────────────
  getUsers(): User[] {
    try {
      const raw = localStorage.getItem(STORAGE_KEYS.USERS);
      return raw ? JSON.parse(raw) : INITIAL_USERS;
    } catch {
      return INITIAL_USERS;
    }
  }

  getActiveUserId(): number {
    try {
      const raw = localStorage.getItem(STORAGE_KEYS.ACTIVE_USER_ID);
      const id = raw ? parseInt(raw, 10) : 1;
      return isNaN(id) ? 1 : id;
    } catch {
      return 1;
    }
  }

  setActiveUserId(id: number) {
    localStorage.setItem(STORAGE_KEYS.ACTIVE_USER_ID, id.toString());
  }

  getActiveUser(): User | null {
    const users = this.getUsers();
    const activeId = this.getActiveUserId();
    return users.find((u) => u.id === activeId) || users[0] || null;
  }

  createUser(name: string, age = 0, avatarColor = '#516351'): User {
    const users = this.getUsers();
    const newId = users.length > 0 ? Math.max(...users.map((u) => u.id)) + 1 : 1;
    const newUser: User = {
      id: newId,
      name: name.trim(),
      age: age || 0,
      avatarColor,
      createdAt: Date.now(),
    };
    const updated = [...users, newUser];
    localStorage.setItem(STORAGE_KEYS.USERS, JSON.stringify(updated));
    this.setActiveUserId(newId);
    return newUser;
  }

  deleteUser(userId: number) {
    let users = this.getUsers().filter((u) => u.id !== userId);
    if (users.length === 0) {
      // If no users left, reset to clean guest or initial
      users = [
        {
          id: 1,
          name: 'You',
          age: 0,
          avatarColor: '#516351',
          createdAt: Date.now(),
        },
      ];
    }
    localStorage.setItem(STORAGE_KEYS.USERS, JSON.stringify(users));

    // Also remove their medications and dose logs
    const meds = this.getMedications().filter((m) => m.userId !== userId);
    localStorage.setItem(STORAGE_KEYS.MEDICATIONS, JSON.stringify(meds));

    const logs = this.getDoseLogs().filter((l) => l.userId !== userId);
    localStorage.setItem(STORAGE_KEYS.DOSE_LOGS, JSON.stringify(logs));

    if (this.getActiveUserId() === userId) {
      this.setActiveUserId(users[0].id);
    }
  }

  // ── Medications ────────────────────────────────────────────────────────────
  getMedications(userId?: number): Medication[] {
    try {
      const raw = localStorage.getItem(STORAGE_KEYS.MEDICATIONS);
      const all: Medication[] = raw ? JSON.parse(raw) : [];
      const uid = userId !== undefined ? userId : this.getActiveUserId();
      return all.filter((m) => m.userId === uid);
    } catch {
      return [];
    }
  }

  getAllMedications(): Medication[] {
    try {
      const raw = localStorage.getItem(STORAGE_KEYS.MEDICATIONS);
      return raw ? JSON.parse(raw) : [];
    } catch {
      return [];
    }
  }

  getMedicationById(id: number): Medication | undefined {
    const all = this.getAllMedications();
    return all.find((m) => m.id === id);
  }

  saveMedication(med: Omit<Medication, 'id' | 'createdAt'> & { id?: number }): Medication {
    const all = this.getAllMedications();
    if (med.id) {
      const index = all.findIndex((m) => m.id === med.id);
      if (index >= 0) {
        const updated: Medication = {
          ...all[index],
          ...med,
          id: med.id,
        };
        all[index] = updated;
        localStorage.setItem(STORAGE_KEYS.MEDICATIONS, JSON.stringify(all));
        return updated;
      }
    }

    const newId = all.length > 0 ? Math.max(...all.map((m) => m.id)) + 1 : 1;
    const newMed: Medication = {
      ...med,
      id: newId,
      createdAt: Date.now(),
    };
    all.push(newMed);
    localStorage.setItem(STORAGE_KEYS.MEDICATIONS, JSON.stringify(all));

    // Generate today's dose logs for this new medication
    this.ensureTodayLogsForMedication(newMed);

    return newMed;
  }

  deleteMedication(id: number) {
    const all = this.getAllMedications().filter((m) => m.id !== id);
    localStorage.setItem(STORAGE_KEYS.MEDICATIONS, JSON.stringify(all));
  }

  toggleMedicationActive(id: number): boolean {
    const all = this.getAllMedications();
    const med = all.find((m) => m.id === id);
    if (!med) return false;
    med.isActive = !med.isActive;
    localStorage.setItem(STORAGE_KEYS.MEDICATIONS, JSON.stringify(all));
    return med.isActive;
  }

  // ── Dose Logs ──────────────────────────────────────────────────────────────
  getDoseLogs(userId?: number): DoseLog[] {
    try {
      const raw = localStorage.getItem(STORAGE_KEYS.DOSE_LOGS);
      const all: DoseLog[] = raw ? JSON.parse(raw) : [];
      const uid = userId !== undefined ? userId : this.getActiveUserId();
      return all.filter((l) => l.userId === uid);
    } catch {
      return [];
    }
  }

  getAllDoseLogs(): DoseLog[] {
    try {
      const raw = localStorage.getItem(STORAGE_KEYS.DOSE_LOGS);
      return raw ? JSON.parse(raw) : [];
    } catch {
      return [];
    }
  }

  updateDoseLogStatus(doseLogId: number, status: DoseStatus, takenAt: number | null = Date.now()): DoseLog | null {
    const all = this.getAllDoseLogs();
    const log = all.find((l) => l.id === doseLogId);
    if (!log) return null;
    log.status = status;
    log.takenAt = status === 'TAKEN' ? (takenAt || Date.now()) : null;
    localStorage.setItem(STORAGE_KEYS.DOSE_LOGS, JSON.stringify(all));
    return log;
  }

  ensureTodayLogsForMedication(med: Medication) {
    if (!med.isActive || med.reminderTimes.length === 0) return;
    const all = this.getAllDoseLogs();
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    for (const time of med.reminderTimes) {
      const [h, m] = time.split(':').map(Number);
      const sched = new Date(today);
      sched.setHours(h, m, 0, 0);
      const schedTime = sched.getTime();

      const exists = all.some(
        (l) => l.medicationId === med.id && Math.abs(l.scheduledTime - schedTime) < 60 * 1000
      );

      if (!exists) {
        const newId = all.length > 0 ? Math.max(...all.map((l) => l.id)) + 1 : 1;
        all.push({
          id: newId,
          medicationId: med.id,
          userId: med.userId,
          scheduledTime: schedTime,
          takenAt: null,
          status: 'PENDING',
          scheduledSlot: time,
        });
      }
    }

    localStorage.setItem(STORAGE_KEYS.DOSE_LOGS, JSON.stringify(all));
  }

  ensureDoseLogsForDate(date: Date, userId?: number): DoseLog[] {
    const uid = userId !== undefined ? userId : this.getActiveUserId();
    const meds = this.getMedications(uid).filter((m) => m.isActive);
    const all = this.getAllDoseLogs();

    const startOfDay = new Date(date);
    startOfDay.setHours(0, 0, 0, 0);
    const endOfDay = new Date(date);
    endOfDay.setHours(23, 59, 59, 999);

    let changed = false;

    for (const med of meds) {
      for (const time of med.reminderTimes) {
        const [h, m] = time.split(':').map(Number);
        const sched = new Date(date);
        sched.setHours(h, m, 0, 0);
        const schedTime = sched.getTime();

        const exists = all.some(
          (l) => l.medicationId === med.id && Math.abs(l.scheduledTime - schedTime) < 60 * 1000
        );

        if (!exists) {
          const newId = all.length > 0 ? Math.max(...all.map((l) => l.id)) + 1 : 1;
          all.push({
            id: newId,
            medicationId: med.id,
            userId: uid,
            scheduledTime: schedTime,
            takenAt: null,
            status: 'PENDING',
            scheduledSlot: time,
          });
          changed = true;
        }
      }
    }

    if (changed) {
      localStorage.setItem(STORAGE_KEYS.DOSE_LOGS, JSON.stringify(all));
    }

    return all.filter(
      (l) => l.userId === uid && l.scheduledTime >= startOfDay.getTime() && l.scheduledTime <= endOfDay.getTime()
    );
  }

  // ── Maintenance ────────────────────────────────────────────────────────────
  seedSampleHistory(): { addedDoses: number } {
    const users = this.getUsers();
    const meds = this.getAllMedications();
    const newLogs = generateSeedDoseLogs(users, meds);
    localStorage.setItem(STORAGE_KEYS.DOSE_LOGS, JSON.stringify(newLogs));
    return { addedDoses: newLogs.length };
  }

  clearDoseHistory(userId?: number) {
    if (userId !== undefined) {
      const remaining = this.getAllDoseLogs().filter((l) => l.userId !== userId);
      localStorage.setItem(STORAGE_KEYS.DOSE_LOGS, JSON.stringify(remaining));
    } else {
      localStorage.setItem(STORAGE_KEYS.DOSE_LOGS, JSON.stringify([]));
    }
  }

  // ── Backup & Restore (Full schema compatibility) ───────────────────────────
  exportBackup(): HavnBackup {
    const users = this.getUsers();
    const allMeds = this.getAllMedications();
    const allLogs = this.getAllDoseLogs();

    const profiles: BackupProfile[] = users.map((user) => {
      const userMeds = allMeds.filter((m) => m.userId === user.id);
      const medIdMap = new Map<number, number>(); // realId -> localId
      let localMedId = 1;

      const backupMeds: BackupMedication[] = userMeds.map((m) => {
        const lid = localMedId++;
        medIdMap.set(m.id, lid);
        return {
          local_id: lid,
          name: m.name,
          dosage: m.dosage,
          reminder_times: m.reminderTimes,
          repeat_type: m.repeatType,
          color_tag: m.colorTag,
          icon_type: m.iconType,
          is_active: m.isActive,
          notes: m.notes,
        };
      });

      const userLogs = allLogs.filter((l) => l.userId === user.id);
      const backupLogs: BackupDoseLog[] = userLogs
        .filter((l) => medIdMap.has(l.medicationId))
        .map((l) => ({
          medication_local_id: medIdMap.get(l.medicationId)!,
          scheduled_time: l.scheduledTime,
          taken_at: l.takenAt,
          status: l.status,
          scheduled_slot: l.scheduledSlot,
        }));

      return {
        name: user.name,
        age: user.age,
        avatar_color: user.avatarColor,
        created_at: user.createdAt,
        medications: backupMeds,
        dose_logs: backupLogs,
      };
    });

    return {
      schema_version: 1,
      app_version: '1.0.1',
      exported_at: Date.now(),
      profiles,
    };
  }

  restoreBackup(backup: HavnBackup): { profiles: number; medications: number; doses: number } {
    if (!backup || !backup.profiles || !Array.isArray(backup.profiles)) {
      throw new Error('Invalid backup file structure.');
    }

    const currentUsers = this.getUsers();
    const currentMeds = this.getAllMedications();
    const currentLogs = this.getAllDoseLogs();

    let nextUserId = currentUsers.length > 0 ? Math.max(...currentUsers.map((u) => u.id)) + 1 : 1;
    let nextMedId = currentMeds.length > 0 ? Math.max(...currentMeds.map((m) => m.id)) + 1 : 1;
    let nextLogId = currentLogs.length > 0 ? Math.max(...currentLogs.map((l) => l.id)) + 1 : 1;

    let restoredProfiles = 0;
    let restoredMeds = 0;
    let restoredDoses = 0;

    for (const bProf of backup.profiles) {
      const newUserId = nextUserId++;
      const newUser: User = {
        id: newUserId,
        name: bProf.name || 'Imported Profile',
        age: bProf.age || 0,
        avatarColor: bProf.avatar_color || '#516351',
        createdAt: bProf.created_at || Date.now(),
      };
      currentUsers.push(newUser);
      restoredProfiles++;

      const localIdToNewRealId = new Map<number, number>();

      for (const bMed of bProf.medications || []) {
        const newMedId = nextMedId++;
        localIdToNewRealId.set(bMed.local_id, newMedId);
        currentMeds.push({
          id: newMedId,
          userId: newUserId,
          name: bMed.name,
          dosage: bMed.dosage || '',
          reminderTimes: bMed.reminder_times || [],
          repeatType: (bMed.repeat_type as any) || 'DAILY',
          colorTag: (bMed.color_tag as any) || 'sage',
          iconType: (bMed.icon_type as any) || 'CAPSULE',
          isActive: bMed.is_active !== undefined ? bMed.is_active : true,
          notes: bMed.notes || '',
          createdAt: Date.now(),
        });
        restoredMeds++;
      }

      for (const bLog of bProf.dose_logs || []) {
        const realMedId = localIdToNewRealId.get(bLog.medication_local_id);
        if (realMedId) {
          currentLogs.push({
            id: nextLogId++,
            medicationId: realMedId,
            userId: newUserId,
            scheduledTime: bLog.scheduled_time,
            takenAt: bLog.taken_at,
            status: (bLog.status as DoseStatus) || 'PENDING',
            scheduledSlot: bLog.scheduled_slot || '',
          });
          restoredDoses++;
        }
      }
    }

    localStorage.setItem(STORAGE_KEYS.USERS, JSON.stringify(currentUsers));
    localStorage.setItem(STORAGE_KEYS.MEDICATIONS, JSON.stringify(currentMeds));
    localStorage.setItem(STORAGE_KEYS.DOSE_LOGS, JSON.stringify(currentLogs));

    return {
      profiles: restoredProfiles,
      medications: restoredMeds,
      doses: restoredDoses,
    };
  }
}

export const storageService = StorageService.getInstance();
