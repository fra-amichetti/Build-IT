import React from 'react';

type BadgeVariant = 'gray' | 'blue' | 'red' | 'green' | 'amber';

interface StatusBadgeProps {
  status: string;
  variant?: BadgeVariant;
  size?: 'sm' | 'md' | 'lg';
}

const variantStyles: Record<BadgeVariant, string> = {
  gray: 'bg-gray-100 text-gray-700 border-gray-200',
  blue: 'bg-blue-100 text-blue-700 border-blue-200',
  red: 'bg-red-100 text-red-700 border-red-200',
  green: 'bg-green-100 text-green-700 border-green-200',
  amber: 'bg-red-100 text-red-800 border-red-200',
};

const sizeStyles = {
  sm: 'px-2 py-0.5 text-xs',
  md: 'px-2.5 py-1 text-sm',
  lg: 'px-3 py-1.5 text-base',
};

export function StatusBadge({ status, variant = 'gray', size = 'md' }: StatusBadgeProps) {
  return (
    <span
      className={`inline-flex items-center font-medium rounded-full border ${variantStyles[variant]} ${sizeStyles[size]}`}
    >
      {status}
    </span>
  );
}

// Helper to get variant based on status text
export function getSiteStatusVariant(status: string): BadgeVariant {
  switch (status) {
    case 'Pianificato':
      return 'gray';
    case 'In Corso':
      return 'blue';
    case 'In Ritardo':
      return 'red';
    case 'Terminato':
      return 'green';
    default:
      return 'gray';
  }
}

export function getPhaseStatusVariant(status: string): BadgeVariant {
  switch (status) {
    case 'Pianificata':
      return 'gray';
    case 'In Corso':
      return 'blue';
    case 'In Ritardo':
      return 'red';
    case 'Completata':
      return 'green';
    default:
      return 'gray';
  }
}

export function getPaymentStatusVariant(status: string): BadgeVariant {
  switch (status) {
    case 'Da Saldare':
      return 'amber';
    case 'Saldato':
      return 'green';
    default:
      return 'gray';
  }
}
