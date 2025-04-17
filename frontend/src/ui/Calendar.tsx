import React, { useEffect, useState } from 'react';
import './calendar.css';

interface Assignment {
  id: number;
  name: string;
  due_at: string | null;
  courseName: string;
}

// Színpaletta (Tailwind CSS ihletésű, 12 szín)
const colorPalette = [
  '#3B82F6', // Kék
  '#10B981', // Zöld
  '#8B5CF6', // Lila
  '#EF4444', // Piros
  '#F97316', // Narancs
  '#06B6D4', // Türkiz
  '#EAB308', // Sárga
  '#EC4899', // Rózsaszín
  '#6366F1', // Indigó
  '#059669', // Smaragd
  '#14B8A6', // Kékeszöld
  '#F87171', // Korall
];

// Egyszerű hash függvény a kurzusnévből színt választani
const getCourseColor = (courseName: string) => {
  let hash = 0;
  for (let i = 0; i < courseName.length; i++) {
    hash = courseName.charCodeAt(i) + ((hash << 5) - hash);
  }
  const index = Math.abs(hash) % colorPalette.length;
  return colorPalette[index];
};

// Szöveg szín az olvashatóság érdekében
const getTextColor = (bgColor: string) => {
  const lightColors = ['#EAB308', '#F97316', '#FBBF24', '#FCD34D'];
  return lightColors.includes(bgColor) ? '#1a1a1a' : '#fff';
};

const Calendar: React.FC = () => {
  const [assignments, setAssignments] = useState<Assignment[]>([]);
  const [currentDate, setCurrentDate] = useState(new Date());
  const [selectedCourse, setSelectedCourse] = useState<string | null>(null); // Kiválasztott kurzus

  useEffect(() => {
    fetch('http://localhost:8080/canvas/calendar', {
      headers: {
        Authorization: 'Bearer OKKUnqKfWy2hDJn8QbwEWqG37T8fAC7BhGQfYJkiH1bg3ZxJWweLsLYCu2CCjdC0',
      },
    })
      .then((response) => {
        if (!response.ok) throw new Error('Hiba a feladatok lekérése közben');
        return response.json();
      })
      .then((data: Assignment[]) => setAssignments(data))
      .catch((error) => console.error('Hiba a feladatok lekérése közben:', error));
  }, []);

  const firstDayOfMonth = new Date(currentDate.getFullYear(), currentDate.getMonth(), 1);
  const lastDayOfMonth = new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 0);
  const daysInMonth = lastDayOfMonth.getDate();
  const firstDayWeekday = firstDayOfMonth.getDay() || 7;

  const prevMonth = () => setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1));
  const nextMonth = () => setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 1));

  const renderDay = (day: number) => {
    const date = new Date(currentDate.getFullYear(), currentDate.getMonth(), day);
    const dayAssignments = assignments.filter((assignment) => {
      if (!assignment.due_at) return false;
      const dueDate = new Date(assignment.due_at);
      return (
        dueDate.getFullYear() === date.getFullYear() &&
        dueDate.getMonth() === date.getMonth() &&
        dueDate.getDate() === day
      );
    });

    return (
      <div className="day" key={day}>
        <strong>{day}</strong>
        {dayAssignments.map((assignment) => {
          const bgColor = getCourseColor(assignment.courseName);
          const textColor = getTextColor(bgColor);
          return (
            <div
              key={assignment.id}
              className="event"
              style={{ backgroundColor: bgColor, color: textColor }}
              onClick={() => setSelectedCourse(assignment.courseName)}
            >
              {assignment.name} (
              {new Date(assignment.due_at!).toLocaleTimeString('hu-HU', { hour: '2-digit', minute: '2-digit' })})
            </div>
          );
        })}
      </div>
    );
  };

  const emptyDays = Array.from({ length: firstDayWeekday - 1 }, (_, i) => (
    <div className="day empty" key={`empty-${i}`} />
  ));

  const calendarDays = Array.from({ length: daysInMonth }, (_, i) => renderDay(i + 1));

  return (
    <div>
      <div className="calendar-header">
        <button onClick={prevMonth}>Előző hónap</button>
        <h2>
          {currentDate.toLocaleString('hu-HU', { year: 'numeric', month: 'long' })}
        </h2>
        <button onClick={nextMonth}>Következő hónap</button>
      </div>
      <div id="calendar">
        {/* Fejlécek a hét napjaihoz */}
        <div className="day-header">Hétfő</div>
        <div className="day-header">Kedd</div>
        <div className="day-header">Szerda</div>
        <div className="day-header">Csütörtök</div>
        <div className="day-header">Péntek</div>
        <div className="day-header">Szombat</div>
        <div className="day-header">Vasárnap</div>
        {/* Üres napok az első hét előtt */}
        {emptyDays}
        {/* Napok renderelése */}
        {calendarDays}
      </div>
      {/* Modal a kiválasztott kurzus jelmagyarázatához */}
      {selectedCourse && (
        <div className="modal-overlay" onClick={() => setSelectedCourse(null)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h3>Kurzus jelmagyarázata</h3>
            <div className="legend">
              <div>
                <span
                  style={{
                    backgroundColor: getCourseColor(selectedCourse),
                    color: getTextColor(getCourseColor(selectedCourse)),
                  }}
                >
                  ⬤
                </span>
                {selectedCourse}
              </div>
            </div>
            <button className="modal-close" onClick={() => setSelectedCourse(null)}>
              Bezárás
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default Calendar;