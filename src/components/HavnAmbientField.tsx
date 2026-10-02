import React from 'react';
import { motion } from 'motion/react';

interface HavnAmbientFieldProps {
  className?: string;
}

export const HavnAmbientField: React.FC<HavnAmbientFieldProps> = ({ className = '' }) => {
  return (
    <div
      className={`pointer-events-none absolute inset-0 overflow-hidden select-none opacity-40 dark:opacity-25 ${className}`}
      aria-hidden="true"
    >
      <div className="absolute -top-[20%] left-1/2 -translate-x-1/2 w-[550px] h-[550px] rounded-full bg-radial from-[#516351]/20 via-[#9A5637]/10 to-transparent blur-3xl" />
      <div className="absolute top-[35%] -right-[15%] w-[450px] h-[450px] rounded-full bg-radial from-[#8A6B22]/15 via-[#516351]/10 to-transparent blur-3xl" />
    </div>
  );
};

interface HavnBreathingMarkProps {
  size?: number;
  className?: string;
}

export const HavnBreathingMark: React.FC<HavnBreathingMarkProps> = ({
  size = 64,
  className = '',
}) => {
  return (
    <div
      style={{ width: size, height: size }}
      className={`relative flex items-center justify-center select-none ${className}`}
    >
      <motion.div
        animate={{
          scale: [1, 1.22, 1],
          opacity: [0.35, 0.75, 0.35],
        }}
        transition={{
          duration: 4.5,
          repeat: Infinity,
          ease: 'easeInOut',
        }}
        className="absolute inset-0 rounded-full border border-[#516351] dark:border-[#7B947B]"
      />
      <motion.div
        animate={{
          scale: [0.9, 1.1, 0.9],
          opacity: [0.2, 0.5, 0.2],
        }}
        transition={{
          duration: 4.5,
          repeat: Infinity,
          ease: 'easeInOut',
          delay: 0.5,
        }}
        className="absolute inset-2 rounded-full bg-[#516351]/10 dark:bg-[#7B947B]/15"
      />
      <div className="w-2.5 h-2.5 rounded-full bg-[#516351] dark:bg-[#7B947B]" />
    </div>
  );
};
