import React from 'react';
import { motion } from 'motion/react';
import { soundManager } from '../audio/soundManager';

interface HavnSwitchProps {
  checked: boolean;
  onCheckedChange: (checked: boolean) => void;
  contentDescription?: string;
  disabled?: boolean;
}

export const HavnSwitch: React.FC<HavnSwitchProps> = ({
  checked,
  onCheckedChange,
  contentDescription,
  disabled = false,
}) => {
  const toggle = () => {
    if (disabled) return;
    soundManager.playCeramicClick();
    onCheckedChange(!checked);
  };

  return (
    <button
      type="button"
      role="switch"
      aria-checked={checked}
      aria-label={contentDescription}
      disabled={disabled}
      onClick={toggle}
      className={`relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full p-0.5 transition-colors duration-200 ease-in-out focus:outline-hidden ${
        checked
          ? 'bg-[#516351] dark:bg-[#7B947B]'
          : 'bg-[#ECEBE4] dark:bg-[#2A2E27]'
      } ${disabled ? 'opacity-40 cursor-not-allowed' : ''}`}
    >
      <motion.span
        layout
        transition={{ type: 'spring', stiffness: 500, damping: 30 }}
        className={`pointer-events-none block h-5 w-5 rounded-full bg-white shadow-xs ${
          checked ? 'translate-x-5' : 'translate-x-0'
        }`}
      />
    </button>
  );
};

interface HavnSwitchRowProps {
  title: string;
  subtitle?: string;
  checked: boolean;
  onCheckedChange: (checked: boolean) => void;
  onRowClick?: () => void;
}

export const HavnSwitchRow: React.FC<HavnSwitchRowProps> = ({
  title,
  subtitle,
  checked,
  onCheckedChange,
  onRowClick,
}) => {
  return (
    <div
      onClick={onRowClick}
      className={`flex items-center justify-between py-3.5 border-b border-[#E2E1D9] dark:border-[#292E26] ${
        onRowClick ? 'cursor-pointer' : ''
      }`}
    >
      <div className="flex-1 pr-4">
        <h4 className="text-sm font-medium text-[#141613] dark:text-[#EDEDEA]">{title}</h4>
        {subtitle && (
          <p className="text-xs text-[#8C9287] dark:text-[#73796E] mt-0.5">{subtitle}</p>
        )}
      </div>
      <HavnSwitch
        checked={checked}
        onCheckedChange={onCheckedChange}
        contentDescription={title}
      />
    </div>
  );
};

interface HavnSegmentedProps {
  options: string[];
  selectedIndex: number;
  onSelect: (index: number) => void;
  className?: string;
}

export const HavnSegmented: React.FC<HavnSegmentedProps> = ({
  options,
  selectedIndex,
  onSelect,
  className = '',
}) => {
  return (
    <div
      className={`flex p-1 bg-[#ECEBE4] dark:bg-[#1C1F1A] rounded-xl border border-[#E2E1D9] dark:border-[#292E26] ${className}`}
    >
      {options.map((option, index) => {
        const isSelected = index === selectedIndex;
        return (
          <button
            key={option}
            type="button"
            onClick={() => {
              if (index !== selectedIndex) {
                soundManager.playSoftTap();
                onSelect(index);
              }
            }}
            className={`relative flex-1 py-2 text-xs font-medium tracking-wide rounded-lg transition-colors select-none cursor-pointer text-center ${
              isSelected
                ? 'text-[#141613] dark:text-[#EDEDEA]'
                : 'text-[#8C9287] dark:text-[#73796E] hover:text-[#5E645A]'
            }`}
          >
            {isSelected && (
              <motion.div
                layoutId="segmentedIndicator"
                transition={{ type: 'spring', stiffness: 450, damping: 35 }}
                className="absolute inset-0 bg-[#FFFFFF] dark:bg-[#282E25] rounded-lg shadow-xs"
              />
            )}
            <span className="relative z-10">{option}</span>
          </button>
        );
      })}
    </div>
  );
};
