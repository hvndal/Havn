export type MedIconType =
  | 'CAPSULE'
  | 'TABLET'
  | 'LIQUID'
  | 'INJECTION'
  | 'DROPS'
  | 'INHALER'
  | 'PATCH';

export type RepeatType = 'DAILY' | 'WEEKLY' | 'AS_NEEDED';

export type ColorTag = 'sage' | 'clay' | 'amber' | 'slate' | 'sand';

export type DoseStatus = 'PENDING' | 'TAKEN' | 'SNOOZED' | 'SKIPPED';

export type SlotLabel = 'MORNING' | 'AFTERNOON' | 'EVENING' | 'NIGHT';

export type ThemeMode = 'light' | 'dark' | 'system';

export interface User {
  id: number;
  name: string;
  age: number;
  avatarColor: string;
  createdAt: number;
}

export interface Medication {
  id: number;
  userId: number;
  name: string;
  dosage: string;
  reminderTimes: string[]; // e.g. ["08:00", "20:00"]
  repeatType: RepeatType;
  colorTag: ColorTag;
  iconType: MedIconType;
  isActive: boolean;
  notes: string;
  createdAt: number;
}

export interface DoseLog {
  id: number;
  medicationId: number;
  userId: number;
  scheduledTime: number; // timestamp in ms
  takenAt: number | null; // timestamp in ms
  status: DoseStatus;
  scheduledSlot: string; // "08:00" or period name
}

export interface TodayDose {
  doseLogId: number;
  medication: Medication;
  scheduledTime: number;
  scheduledSlot: string;
  status: DoseStatus;
  takenAt: number | null;
}

export interface OrganizerSlotPill {
  id: string;
  medicationId: number;
  doseLogId: number;
  name: string;
  color: string;
  secondaryColor: string;
  shape: 'capsule' | 'tablet' | 'softgel';
  isTaken: boolean;
}

export interface OrganizerSlotData {
  label: SlotLabel;
  period: string;
  isCompleted: boolean;
  isCurrent: boolean;
  pills: OrganizerSlotPill[];
}

export interface UserPreferences {
  theme: ThemeMode;
  interfaceSound: boolean;
  preDoseEnabled: boolean;
  eveningCheckEnabled: boolean;
  eveningCheckTime: string;
  vibration: boolean;
  sound: 'CHIME' | 'MARIMBA' | 'SILENT';
}

export interface BackupMedication {
  local_id: number;
  name: string;
  dosage: string;
  reminder_times: string[];
  repeat_type: string;
  color_tag: string;
  icon_type: string;
  is_active: boolean;
  notes: string;
}

export interface BackupDoseLog {
  medication_local_id: number;
  scheduled_time: number;
  taken_at: number | null;
  status: string;
  scheduled_slot: string;
}

export interface BackupProfile {
  name: string;
  age: number;
  avatar_color: string;
  created_at: number;
  medications: BackupMedication[];
  dose_logs: BackupDoseLog[];
}

export interface HavnBackup {
  schema_version: number;
  app_version: string;
  exported_at: number;
  profiles: BackupProfile[];
}
