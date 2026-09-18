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
        <label className="text-[11px] font-semibold tracking-[0.15em] text-[#8C9287] dark:text-[#73796E] uppercase mb-1.5 font-sans">
          {label}
        </label>
      )}

      {singleLine ? (
        <input
          value={value}
          onChange={onChange}
          placeholder={placeholder}
          disabled={disabled}
          className={`w-full px-4 py-3 text-sm rounded-xl bg-[#ECEBE4] dark:bg-[#1C1F1A] text-[#141613] dark:text-[#EDEDEA] placeholder-[#8C9287]/60 dark:placeholder-[#73796E]/60 border border-transparent focus:border-[#516351] dark:focus:border-[#7B947B] focus:outline-hidden transition-all duration-150 ${
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
          className={`w-full px-4 py-3 text-sm rounded-xl bg-[#ECEBE4] dark:bg-[#1C1F1A] text-[#141613] dark:text-[#EDEDEA] placeholder-[#8C9287]/60 dark:placeholder-[#73796E]/60 border border-transparent focus:border-[#516351] dark:focus:border-[#7B947B] focus:outline-hidden transition-all duration-150 resize-none ${
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
