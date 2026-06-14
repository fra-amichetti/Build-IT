import { LogOut, Menu, User } from 'lucide-react';
import { useApp } from '../../context/AppContext';

interface HeaderProps {
  onMenuClick?: () => void;
  showMenuButton?: boolean;
}

export function Header({ onMenuClick, showMenuButton = false }: HeaderProps) {
  const { currentUser, logout } = useApp();

  return (
    <header className="sticky top-0 z-40 bg-white border-b border-gray-200 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <div className="flex items-center gap-3">
            {showMenuButton && (
              <button
                onClick={onMenuClick}
                className="lg:hidden p-2 rounded-lg text-gray-600 hover:bg-gray-100 transition-colors"
              >
                <Menu className="w-6 h-6" />
              </button>
            )}
            <div className="flex items-center gap-2">
              <img
                src="/icon-house.png"
                alt="Build-IT House Icon"
                className="h-10 w-auto"
              />
              <img
                src="/text-buildit.png"
                alt="Build-IT Text"
                className="h-6 w-auto"
              />
            </div>
          </div>

          {/* User Info & Actions */}
          {currentUser && (
            <div className="flex items-center gap-3">
              <div className="hidden sm:flex flex-col items-end">
                <span className="text-sm font-medium text-gray-900">
                  {currentUser.nome} {currentUser.cognome}
                </span>
                <span className="text-xs text-red-700 font-medium">{currentUser.role}</span>
              </div>

              <div className="w-10 h-10 rounded-full bg-red-100 flex items-center justify-center">
                <User className="w-5 h-5 text-red-700" />
              </div>

              <button
                onClick={logout}
                className="flex items-center gap-2 px-3 py-2 text-sm font-medium text-gray-600 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
              >
                <LogOut className="w-4 h-4" />
                <span className="hidden sm:inline">Esci</span>
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
