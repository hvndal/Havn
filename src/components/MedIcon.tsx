import React from 'react';
import { Pill, Disc, Droplets, Syringe, Droplet, Wind, Square } from 'lucide-react';
import { MedIconType } from '../types';

interface MedIconProps {
  type: MedIconType;
  size?: number;
  className?: string;
  color?: string;
}

export const MedIcon: React.FC<MedIconProps> = ({
  type,
  size = 20,
  className = '',
  color,
}) => {
  const iconProps = {
    size,
    className,
    style: color ? { color } : undefined,
    strokeWidth: 1.8,
  };

  switch (type) {
    case 'TABLET':
      return <Disc {...iconProps} />;
    case 'LIQUID':
      return <Droplets {...iconProps} />;
    case 'INJECTION':
      return <Syringe {...iconProps} />;
    case 'DROPS':
      return <Droplet {...iconProps} />;
    case 'INHALER':
      return <Wind {...iconProps} />;
    case 'PATCH':
      return <Square {...iconProps} />;
    case 'CAPSULE':
    default:
      return <Pill {...iconProps} />;
  }
};
