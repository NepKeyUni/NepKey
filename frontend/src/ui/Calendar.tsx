import React, { useEffect, useState } from 'react';
import './calendar.css';

interface Assignment {
  id: number;
  name: string;
  due_at: string | null;
  courseId: number;
  courseName: string;
}

interface Course {
  id: number;
  name: string;
}

const Calendar: React.FC = () => {
  const [assignments, setAssignments] = useState<Assignment[]>([]);
  const [courses, setCourses] = useState<Course[]>([]);
  const [selectedCourses, setSelectedCourses] = useState<number[]>([]);
  const [currentDate, setCurrentDate] = useState(new Date()); // Aktuális hónap

  // Kurzusok és feladatok lekérése
  useEffect(() => {
    // Kurzusok
    fetch('http://localhost:8080/canvas/courses', {
      headers: {
        Authorization: 'Bearer OKKUnqKfWy2hDJn8QbwEWqG37T8fAC7BhGQfYJkiH1bg3ZxJWweLsLYCu2CCjdC0',
      },
    })
      .then((response) => {
        if (!response.ok) throw new Error('Hiba a kurzusok lekérése közben');
        return response.json();
      })
      .then((data: Course[]) => {
        setCourses(data);
        setSelectedCourses(data.map((course) => course.id)); // Alapértelmezetten minden kurzus
      })
      .catch((error) => console.error('Hiba a kurzusok lekérése közben:', error));

    // Feladatok
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

  // Kurzus szűrés
  const toggleCourse = (courseId: number) => {
    setSelectedCourses((prev) =>
      prev.includes(courseId) ? prev.filter((id) => id !== courseId) : [...prev, courseId]
    );
  };

  // Hónap első és utolsó napja
  const firstDayOfMonth = new Date(currentDate.getFullYear(), currentDate.getMonth(), 1);
  const lastDayOfMonth = new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 0);
  const daysInMonth = lastDayOfMonth.getDate();
  const firstDayWeekday = firstDayOfMonth.getDay() || 7;

  // Hónap lapozás
  const prevMonth = () => setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1));
  const nextMonth = () => setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 1));

  // Naptár napjainak renderelése
  const renderDay = (day: number) => {
    const date = new Date(currentDate.getFullYear(), currentDate.getMonth(), day);
    const dayAssignments = assignments.filter((assignment) => {
      if (!assignment.due_at || !selectedCourses.includes(assignment.courseId)) return false;
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
        {dayAssignments.map((assignment) => (
          <div key={assignment.id} className="event">
            {assignment.courseName}: {assignment.name} (
            {new Date(assignment.due_at!).toLocaleTimeString('hu-HU', { hour: '2-digit', minute: '2-digit' })})
          </div>
        ))}
      </div>
    );
  };

  // Üres napok
  const emptyDays = Array.from({ length: firstDayWeekday - 1 }, (_, i) => (
    <div className="day empty" key={`empty-${i}`} />
  ));

  const calendarDays = Array.from({ length: daysInMonth }, (_, i) => renderDay(i + 1));

  // Null határidejű feladatok
  const noDueAssignments = assignments.filter(
    (assignment) => !assignment.due_at && selectedCourses.includes(assignment.courseId)
  );

  return (
    <div>
      <div className="calendar-header">
        <button onClick={prevMonth}>Előző hónap</button>
        <h2>{currentDate.toLocaleString('hu-HU', { year: 'numeric', month: 'long' })}</h2>
        <button onClick={nextMonth}>Következő hónap</button>
      </div>
      <div className="course-filter">
        <h3>Kurzusok kiválasztása</h3>
        {courses.length === 0 && <p>Nincsenek elérhető kurzusok.</p>}
        {courses.map((course) => (
          <label key={course.id} className="course-checkbox">
            <input
              type="checkbox"
              checked={selectedCourses.includes(course.id)}
              onChange={() => toggleCourse(course.id)}
            />
            {course.name}
          </label>
        ))}
      </div>
      <div id="calendar">
        <div className="day-header">Hétfő</div>
        <div className="day-header">Kedd</div>
        <div className="day-header">Szerda</div>
        <div className="day-header">Csütörtök</div>
        <div className="day-header">Péntek</div>
        <div className="day-header">Szombat</div>
        <div className="day-header">Vasárnap</div>
        {emptyDays}
        {calendarDays}
      </div>
      {noDueAssignments.length > 0 && (
        <div id="no-due-assignments">
          <h2>Nincs határidő</h2>
          {noDueAssignments.map((assignment) => (
            <div key={assignment.id} className="event">
              {assignment.courseName}: {assignment.name}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default Calendar;