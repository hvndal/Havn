import React from 'react';
import { motion } from 'motion/react';
import { soundManager } from '../audio/soundManager';

export type HavnButtonTone = 'primary' | 'secondary' | 'ghost' | 'danger';
export type HavnButtonSize = 'small' | 'medium' | 'large';

interface HavnButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  text?: string;
  tone?: HavnButtonTone;
  size?: HavnButtonSize;
  loading?: boolean;
  fillWidth?: boolean;
  children?: React.ReactNode;
}

export const HavnButton: React.FC<HavnButtonProps> = ({
  text,
  tone = 'primary',
  size = 'large',
  loading = false,
  fillWidth = true,
  children,
  onClick,
  disabled,
  className = '',
  ...props
}) => {
  const handleClick = (e: React.MouseEvent<HTMLButtonElement>) => {
    if (disabled || loading) return;
    soundManager.playSoftTap();
    onClick?.(e);
  };

  const toneClasses = {
    primary:
      'bg-[#4E614E] text-[#FAF8F5] dark:bg-[#7B947B] dark:text-[#0C120C] border border-[#415341]/40 dark:border-[#8BA58B]/50 shadow-[0_6px_20px_-3px_rgba(78,97,78,0.32),0_2px_6px_rgba(0,0,0,0.06)] hover:shadow-[0_10px_26px_-3px_rgba(78,97,78,0.42)] active:opacity-95',
    secondary:
      'bg-[#F4F3EE] text-[#171A15] dark:bg-[#1E231D] dark:text-[#EDEDEA] border border-[#141613]/[0.07] dark:border-white/[0.08] shadow-[0_2px_8px_rgba(0,0,0,0.02)] hover:bg-[#EAE8E0] dark:hover:bg-[#262B23]',
    ghost:
      'bg-transparent text-[#5C6357] dark:text-[#A1A79C] hover:bg-[#F4F3EE]/80 dark:hover:bg-[#1E231D]/80 border border-transparent hover:border-[#141613]/[0.05] dark:hover:border-white/[0.06]',
    danger:
      'bg-[#F8E8E2] text-[#9A5637] dark:bg-[#2F1B14] dark:text-[#F2B59D] border border-[#9A5637]/15 dark:border-[#D97A52]/20 hover:opacity-90',
  }[tone];

  const sizeClasses = {
    small: 'h-9 px-3.5 text-xs font-semibold rounded-[12px]',
    medium: 'h-11 px-5 text-sm font-semibold rounded-[16px]',
    large: 'h-13 px-6 text-base font-semibold rounded-[20px]',
  }[size];

  return (
    <motion.button
      whileHover={{ scale: disabled || loading ? 1 : 1.015, y: disabled || loading ? 0 : -0.5 }}
      whileTap={{ scale: disabled || loading ? 1 : 0.96 }}
      transition={{ type: 'spring', stiffness: 340, damping: 22, mass: 0.6 }}
      onClick={handleClick}
      disabled={disabled || loading}
      className={`inline-flex items-center justify-center font-sans tracking-wide transition-colors select-none ${
        fillWidth ? 'w-full' : ''
      } ${toneClasses} ${sizeClasses} ${
        disabled ? 'opacity-40 cursor-not-allowed' : 'cursor-pointer'
      } ${className}`}
      {...(props as any)}
    >
      {loading ? (
        <span className="flex items-center gap-2">
          <span className="w-4 h-4 border-2 border-current border-t-transparent rounded-full animate-spin" />
          <span>Saving…</span>
        </span>
      ) : (
        children || text
      )}
    </motion.button>
  );
};

interface HavnIconButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  icon: React.ReactNode;
  contentDescription?: string;
  size?: number;
}

export const HavnIconButton: React.FC<HavnIconButtonProps> = ({
  icon,
  contentDescription,
  size = 40,
  onClick,
  className = '',
  ...props
}) => {
  const handleClick = (e: React.MouseEvent<HTMLButtonElement>) => {
    soundManager.playSoftTap();
    onClick?.(e);
  };

  return (
    <motion.button
      whileHover={{ scale: 1.06, y: -0.5 }}
      whileTap={{ scale: 0.92 }}
      transition={{ type: 'spring', stiffness: 380, damping: 24, mass: 0.5 }}
      onClick={handleClick}
      aria-label={contentDescription}
      style={{ width: size, height: size }}
      className={`rounded-[14px] flex items-center justify-center bg-[#F4F3EE]/90 dark:bg-[#1E231D]/90 border border-[#141613]/[0.06] dark:border-white/[0.08] text-[#171A15] dark:text-[#EDEDEA] hover:bg-[#EAE8E0] dark:hover:bg-[#262B23] shadow-xs transition-colors cursor-pointer select-none ${className}`}
      {...(props as any)}
    >
      {icon}
    </motion.button>
  );
};
