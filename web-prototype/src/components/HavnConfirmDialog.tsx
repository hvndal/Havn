import React from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { HavnButton } from './HavnButton';

interface HavnConfirmDialogProps {
  isOpen?: boolean;
  title: string;
  body: string;
  confirmLabel?: string;
  cancelLabel?: string;
  destructive?: boolean;
  onConfirm: () => void;
  onDismiss: () => void;
}

export const HavnConfirmDialog: React.FC<HavnConfirmDialogProps> = ({
  isOpen = true,
  title,
  body,
  confirmLabel = 'Confirm',
  cancelLabel = 'Cancel',
  destructive = false,
  onConfirm,
  onDismiss,
}) => {
  if (!isOpen) return null;

  return (
    <AnimatePresence>
      <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-xs">
        <motion.div
          initial={{ opacity: 0, scale: 0.95, y: 10 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          exit={{ opacity: 0, scale: 0.95, y: 10 }}
          transition={{ duration: 0.18, ease: [0.16, 1, 0.3, 1] }}
          className="w-full max-w-sm p-6 bg-[#FFFFFF] dark:bg-[#1C1F1A] rounded-2xl shadow-xl border border-[#E2E1D9] dark:border-[#292E26]"
        >
          <h3 className="text-lg font-semibold text-[#141613] dark:text-[#EDEDEA]">
            {title}
          </h3>
          <p className="mt-2 text-sm text-[#5E645A] dark:text-[#A3A89F] leading-relaxed">
            {body}
          </p>

          <div className="mt-6 flex items-center gap-3">
            <HavnButton
              text={cancelLabel}
              tone="ghost"
              size="medium"
              onClick={onDismiss}
              className="flex-1"
            />
            <HavnButton
              text={confirmLabel}
              tone={destructive ? 'danger' : 'primary'}
              size="medium"
              onClick={onConfirm}
              className="flex-1"
            />
          </div>
        </motion.div>
      </div>
    </AnimatePresence>
  );
};
