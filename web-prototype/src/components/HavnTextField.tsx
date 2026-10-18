import React from 'react';

interface HavnTextFieldProps extends React.InputHTMLAttributes<HTMLInputElement | HTMLTextAreaElement> {
  label?: string;
  placeholder?: string;
  error?: string | null;
  singleLine?: boolean;
  minHeight?: string | number;
}

export const HavnTextField: React.FC<HavnTextFieldProps> = ({
  label,
  placeholder,
  error,
  singleLine = true,
  minHeight,
  value,
  onChange,
  className = '',
  disabled = false,
  ...props
}) => {
  return (
    <div className={`w-full flex flex-col ${className}`}>
      {label && (
        <label className="text-[11px] font-bold tracking-[0.2em] text-[#7A8174] dark:text-[#888E83] uppercase mb-1.5 font-sans">
          {label}
        </label>
      )}

      {singleLine ? (
        <input
          value={value}
          onChange={onChange}
          placeholder={placeholder}
          disabled={disabled}
          className={`w-full px-4 py-3 text-sm rounded-[16px] bg-[#F4F3EE] dark:bg-[#1A1E18] text-[#171A15] dark:text-[#EDEDEA] placeholder-[#7A8174]/60 dark:placeholder-[#888E83]/60 border border-[#141613]/[0.06] dark:border-white/[0.07] focus:border-[#4E614E] dark:focus:border-[#7B947B] focus:outline-hidden transition-all duration-200 shadow-[inset_0_1px_2px_rgba(0,0,0,0.02)] ${
            error ? 'border-[#9A5637] dark:border-[#D97A52]' : ''
          } ${disabled ? 'opacity-50 cursor-not-allowed' : ''}`}
          {...(props as any)}
        />
      ) : (
        <textarea
          value={value}
          onChange={onChange as any}
          placeholder={placeholder}
          disabled={disabled}
          style={minHeight ? { minHeight } : undefined}
          className={`w-full px-4 py-3 text-sm rounded-[16px] bg-[#F4F3EE] dark:bg-[#1A1E18] text-[#171A15] dark:text-[#EDEDEA] placeholder-[#7A8174]/60 dark:placeholder-[#888E83]/60 border border-[#141613]/[0.06] dark:border-white/[0.07] focus:border-[#4E614E] dark:focus:border-[#7B947B] focus:outline-hidden transition-all duration-200 resize-none shadow-[inset_0_1px_2px_rgba(0,0,0,0.02)] ${
            error ? 'border-[#9A5637] dark:border-[#D97A52]' : ''
          } ${disabled ? 'opacity-50 cursor-not-allowed' : ''}`}
          {...(props as any)}
        />
      )}

      {error && (
        <span className="text-xs text-[#9A5637] dark:text-[#D97A52] mt-1.5 font-medium">
          {error}
        </span>
      )}
    </div>
  );
};
