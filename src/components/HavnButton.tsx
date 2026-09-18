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
      'bg-[#516351] text-[#FBFBFA] dark:bg-[#7B947B] dark:text-[#0E130E] hover:opacity-95 active:opacity-90 shadow-xs',
    secondary:
      'bg-[#ECEBE4] text-[#141613] dark:bg-[#242822] dark:text-[#EDEDEA] hover:bg-[#E2E1D9] dark:hover:bg-[#2E332C]',
    ghost:
      'bg-transparent text-[#5E645A] dark:text-[#A3A89F] hover:bg-[#ECEBE4]/50 dark:hover:bg-[#242822]/50',
    danger:
      'bg-[#F8E8E2] text-[#9A5637] dark:bg-[#2F1B14] dark:text-[#F2B59D] hover:opacity-90',
  }[tone];

  const sizeClasses = {
    small: 'h-9 px-3.5 text-xs font-medium rounded-lg',
    medium: 'h-11 px-5 text-sm font-medium rounded-xl',
    large: 'h-13 px-6 text-base font-medium rounded-2xl',
  }[size];

  return (
    <motion.button
      whileTap={{ scale: disabled ? 1 : 0.98 }}
      transition={{ duration: 0.1 }}
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
      whileTap={{ scale: 0.92 }}
      transition={{ duration: 0.1 }}
      onClick={handleClick}
      aria-label={contentDescription}
      style={{ width: size, height: size }}
      className={`rounded-full flex items-center justify-center bg-[#ECEBE4]/80 dark:bg-[#242822]/80 text-[#141613] dark:text-[#EDEDEA] hover:bg-[#E2E1D9] dark:hover:bg-[#2E332C] transition-colors cursor-pointer select-none ${className}`}
      {...(props as any)}
    >
      {icon}
    </motion.button>
  );
};
