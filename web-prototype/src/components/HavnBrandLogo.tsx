import React from 'react';

interface HavnBrandLogoProps {
  iconSize?: number;
  showWordmark?: boolean;
  showTagline?: boolean;
  className?: string;
  textColor?: string;
  subtextColor?: string;
}

export const HavnBrandLogo: React.FC<HavnBrandLogoProps> = ({
  iconSize = 44,
  showWordmark = true,
  showTagline = false,
  className = '',
  textColor = 'currentColor',
  subtextColor = 'currentColor',
}) => {
  return (
    <div className={`flex flex-col items-center select-none ${className}`}>
      <svg
        width={iconSize}
        height={iconSize}
        viewBox="0 0 64 64"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        className="transition-transform duration-300 hover:scale-105"
      >
        {/* Soft geometric pill-box icon with Scandinavian symmetry */}
        <rect
          x="6"
          y="6"
          width="52"
          height="52"
          rx="16"
          className="stroke-[#516351] dark:stroke-[#7B947B]"
          strokeWidth="3.5"
          fill="none"
        />
        {/* 4-compartment grid subtle indicator */}
        <line
          x1="32"
          y1="12"
          x2="32"
          y2="52"
          className="stroke-[#516351]/40 dark:stroke-[#7B947B]/40"
          strokeWidth="2"
          strokeDasharray="3 3"
        />
        <line
          x1="12"
          y1="32"
          x2="52"
          y2="32"
          className="stroke-[#516351]/40 dark:stroke-[#7B947B]/40"
          strokeWidth="2"
          strokeDasharray="3 3"
        />
        {/* Organic centered zen seed / pill */}
        <ellipse
          cx="32"
          cy="32"
          rx="7"
          ry="12"
          transform="rotate(45 32 32)"
          className="fill-[#516351] dark:fill-[#7B947B]"
        />
        <ellipse
          cx="32"
          cy="32"
          rx="3.5"
          ry="6"
          transform="rotate(45 32 32)"
          className="fill-[#FBFBFA] dark:fill-[#141613]"
        />
      </svg>

      {showWordmark && (
        <div className="mt-2 text-center">
          <span
            style={{ color: textColor }}
            className="text-xl font-medium tracking-[0.2em] uppercase font-sans"
          >
            Hävn
          </span>
          {showTagline && (
            <p
              style={{ color: subtextColor }}
              className="text-[10px] tracking-[0.25em] uppercase font-medium mt-0.5 opacity-60"
            >
              Pill Organiser
            </p>
          )}
        </div>
      )}
    </div>
  );
};
