import { LogOut, Menu, User } from 'lucide-react';

interface HeaderProps {
  onMenuClick?: () => void;
  showMenuButton?: boolean;
}

export function Header({ onMenuClick, showMenuButton = false }: HeaderProps) {
  const session = JSON.parse(sessionStorage.getItem('buildit_session') || 'null');
  const realUser = session?.user ?? null;

  const logoutReal = () => {
    sessionStorage.removeItem('buildit_session');
    window.dispatchEvent(new CustomEvent('buildit_logout'));
  };

  const displayUser = realUser;

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
            
            {/* AGGIUNGI onClick QUI e la classe cursor-pointer */}
            <div 
              className="flex items-center gap-2 cursor-pointer hover:opacity-80 transition-opacity"
              onClick={() => window.dispatchEvent(new CustomEvent('tornaAllaHome'))}
            >
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
          {displayUser && (
            <div className="flex items-center gap-3">
              
              <div className="hidden sm:flex flex-col items-end">
                <span className="text-sm font-medium text-gray-900">
                  {displayUser.nome} {displayUser.cognome}
                </span>
                <span className="text-xs text-red-700 font-medium capitalize">
                  {displayUser.ruolo?.toLowerCase()}
                </span>
              </div>

              <div className="w-10 h-10 rounded-full bg-red-100 flex items-center justify-center">
                <User className="w-5 h-5 text-red-700" />
              </div>
{displayUser.ruolo === 'AMMINISTRATORE' && (
                <button
                  onClick={() => window.dispatchEvent(new CustomEvent('navigaAlLog'))}
                  className="px-3 py-2 text-sm font-medium text-white bg-red-600 hover:bg-red-700 rounded-lg transition-colors"
                >
                  Visualizza Log
                </button>
              )}
              <button
                onClick={logoutReal}
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
