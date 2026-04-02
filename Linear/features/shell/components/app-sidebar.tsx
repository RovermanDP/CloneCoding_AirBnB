"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { shellNavigation } from "@/features/shell/constants";

export function AppSidebar() {
  const pathname = usePathname();

  return (
    <aside className="sidebar">
      <div className="brand">
        <div className="brand-mark">B</div>
        <div className="brand-copy">
          <strong>Butler OS</strong>
          <div className="muted">Linear-inspired operations console</div>
        </div>
      </div>

      <nav className="nav-list" aria-label="Primary navigation">
        {shellNavigation.map((item) => {
          const isActive = pathname === item.href || pathname.startsWith(`${item.href}/`);
          return (
            <Link
              key={item.href}
              className={`nav-link${isActive ? " active" : ""}`}
              href={item.href}
            >
              <strong>{item.label}</strong>
              <span className="nav-description">{item.description}</span>
            </Link>
          );
        })}
      </nav>
    </aside>
  );
}

